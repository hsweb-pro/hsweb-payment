package org.hswebframework.payment.openapi.resolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import org.hswebframework.payment.api.concurrent.DuplicateValidatorManager;
import org.hswebframework.payment.api.crypto.CipherManager;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantServiceConfig;
import org.hswebframework.payment.api.utils.IPUtils;
import org.hswebframework.payment.openapi.OpenApiRequest;
import org.hswebframework.payment.openapi.annotation.OpenApi;
import org.hswebframework.payment.openapi.annotation.OpenApiParam;
import org.hswebframework.payment.openapi.exception.OpenApiException;
import io.vavr.API;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.MDC;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j(topic = "system.open-api")
public class OpenApiRequestResponseResolver
        extends StaticMethodMatcherPointcutAdvisor
        implements HandlerMethodArgumentResolver, HandlerInterceptor,
        ResponseBodyAdvice<Object> {

    @Autowired
    private DuplicateValidatorManager validatorManager;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Autowired
    private CipherManager cipherManager;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantConfigManager configManager;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(OpenApiParam.class) != null;
    }

    @ExceptionHandler(OpenApiException.class)
    @OpenApi(value = "")
    public Map<String, Object> handleException(OpenApiException e) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("success", false);
        map.put("code", e.getCode());
        map.put("message", e.getMessage());
        if (e.getCause() != null) {
            log.error("调用OpenApi异常", e.getCause());
        }
        return map;
    }

    @SneakyThrows
    protected <T> T handleException(Callable<T> supplier) {
        try {
            return supplier.call();
        } catch (ConstraintViolationException e) { //spring的验证器异常
            String msg = e.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((s1, s2) -> s1.concat(",").concat(s2))
                    .orElse(ErrorCode.ILLEGAL_PARAMETERS.getText());
            throw OpenApiException.of(ErrorCode.ILLEGAL_PARAMETERS.createException(msg));
        } catch (ValidationException | javax.validation.ValidationException | IllegalArgumentException e) {
            throw OpenApiException.of(ErrorCode.ILLEGAL_PARAMETERS.createException(e.getMessage()));
        } catch (MethodArgumentNotValidException e) { //springmvc的验证器异常
            String msg = e.getBindingResult().getAllErrors()
                    .stream()
                    .filter(FieldError.class::isInstance)
                    .map(FieldError.class::cast)
                    .map(FieldError::getDefaultMessage)
                    .reduce((s1, s2) -> s1.concat(",").concat(s2))
                    .orElse(ErrorCode.ILLEGAL_PARAMETERS.getText());
            throw OpenApiException.of(ErrorCode.ILLEGAL_PARAMETERS.createException(msg));
        } catch (UnAuthorizedException | AccessDenyException e) {//权限异常
            throw OpenApiException.of(ErrorCode.ACCESS_DENIED.createException());
        } catch (BusinessException e) {
            throw OpenApiException.of(e);
        } catch (org.hswebframework.web.BusinessException e) { //其他业务异常
            throw OpenApiException.of(ErrorCode.BUSINESS_FAILED.createException(e.getCode(), e.getMessage(), e));
        } catch (DuplicateKeyException e) {
            throw OpenApiException.of(ErrorCode.DUPLICATE_REQUEST.createException(e));
        } catch (OpenApiException e) {
            throw e;
        } catch (Throwable e) { //未知异常
            throw OpenApiException.of(ErrorCode.SERVICE_ERROR.createException(e));
        }
    }

    public OpenApiRequestResponseResolver() {
        //通过AOP接管异常
        setAdvice((MethodInterceptor) methodInvocation ->
                handleException(() -> API.unchecked(methodInvocation::proceed).apply()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        try {
            HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            // OpenApiParam parameterAnnotation = parameter.getParameterAnnotation(OpenApiParam.class);

            Class<?> type = parameter.getParameterType();

            return resolveArgument(type, httpServletRequest);
        } catch (OpenApiException e) {
            throw e;
        } catch (Throwable e) {
            throw ErrorCode.SERVICE_ERROR.createException(e);
        }
    }

    @SneakyThrows
    public <T> T resolveArgument(Class<T> argType, HttpServletRequest request) {

        Map<String, String> parameters = request.getParameterMap().entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().length != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        return FastBeanCopier.copy(parameters, argType.newInstance());
    }

    public OpenApiRequest resolveOpenApiRequest(HttpServletRequest request) {
        return resolveArgument(OpenApiRequest.class, request);
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        return AnnotationUtils.findAnnotation(method, OpenApi.class) != null;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getMethodAnnotation(OpenApi.class) != null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        OpenApiRequest apiRequest = resolveOpenApiRequest(((ServletServerHttpRequest) request).getServletRequest());

        Map<String, Object> commonResponse = new TreeMap<>();
        commonResponse.put("merchantId", apiRequest.getMerchantId());
        commonResponse.put("requestId", apiRequest.getRequestId());
        commonResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
        try {
            //将返回值合并到公共返回值
            FastBeanCopier.copy(body, commonResponse);

            //进行签名
            Signature signature = cipherManager.getSignature(Signature.Type.MD5, apiRequest.getMerchantId());
            if (signature != null) {
                String sign = signature.sign(commonResponse);
                commonResponse.put("sign", sign);
            }
        } catch (Throwable e) {
            log.error("响应OpenApi请求失败", e);
        }
        return commonResponse;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            AuthenticationHolder.setCurrentUserId(null);
            HandlerMethod method = ((HandlerMethod) handler);
            OpenApi openApi = method.getMethodAnnotation(OpenApi.class);
            if (openApi != null) {
                handleException(() -> {
                    // request.setAttribute("_IS_OPEN_API_REQUEST", true);
                    @SuppressWarnings("unchecked")
                    Map<String, String> parameters = resolveArgument(TreeMap.class, request);

                    MDC.setContextMap(parameters);

                    log.info("收到OpenApi请求:{}", JSON.toJSONString(parameters));

                    OpenApiRequest apiRequest = FastBeanCopier.copy(parameters, OpenApiRequest::new);
                    BeanValidator.tryValidate(apiRequest);

//                    validatorManager.getValidator("request")
//                            .tryPut(apiRequest.getRequestId(), "重复的OpenApi请求,请检查参数:requestId");

                    Merchant merchant = merchantService.getMerchantById(apiRequest.getMerchantId());
                    if (merchant == null) {
                        throw OpenApiException.of(ErrorCode.MERCHANT_NOT_EXISTS);
                    }
                    MDC.put("userId", merchant.getUserId());
                    MDC.put("sessionId",apiRequest.getRequestId());
                    AuthenticationHolder.setCurrentUserId(merchant.getUserId());

                    if (merchant.getStatus() != MerchantStatus.ACTIVE) {
                        throw OpenApiException.of(ErrorCode.MERCHANT_NOT_ACTIVE);
                    }
                    MerchantServiceConfig serviceConfig = configManager
                            .<MerchantServiceConfig>getConfigList(merchant.getId(), MerchantConfigKey.SUPPORTED_SERVICE)
                            .flatMap(configList -> configList.stream()
                                    .filter(config -> openApi.value().equals(config.getServiceId()))
                                    .findAny())
                            .orElseThrow(AccessDenyException::new);
                    if (!CollectionUtils.isEmpty(serviceConfig.getIpWhiteList())) {
                        String ip = IPUtils.getRealIp(WebUtil.getIpAddr(request));
                        if (!serviceConfig.getIpWhiteList().contains(ip)) {
                            log.warn("商户未在白名单内访问open api: 商户[{}:{}], IP:{}", merchant.getId(), merchant.getName(), ip);
                            throw new AccessDenyException();
                        }
                    }
                    //获取商户对应的签名接口
                    Signature signature = cipherManager.getSignature(Signature.Type.MD5, apiRequest.getMerchantId());
                    if (signature == null) {
                        throw OpenApiException.of(ErrorCode.MERCHANT_CONFIG_ERROR);
                    }
                    String sign = apiRequest.getSign();
                    //sign参数不参与签名
                    parameters.remove("sign");
                    //验签
                    boolean verifySuccess = signature.verify(sign, parameters);
                    if (!verifySuccess) {
                        log.error("商户请求验签失败,请求:\n{}\nsign={}", JSON.toJSONString(parameters, SerializerFeature.PrettyFormat), sign);
                        throw OpenApiException.of(ErrorCode.SIGN_ERROR);
                    }
                    return null;
                });
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.clear();
    }
}
