package com.snxy.pay.config;

import com.snxy.common.response.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by 24398 on 2018/8/31.
 */
@Aspect
@Slf4j
@Component
public class ControllerLoggerAspect {

    @Pointcut("execution(public com.snxy.common.response.ResultData com.snxy.pay..web..*(..))")
    public void executeMethod(){

    }

    @Before(value = "executeMethod()")
    public void beforeMethod(JoinPoint joinPoint){
       HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       log.info("记录日志 content-type:  [{}]",request.getContentType());
       boolean isMultipart = ServletFileUpload.isMultipartContent(request);
       if(!isMultipart){
          Map<String,String []> paramMap = request.getParameterMap();
          StringBuilder sb = new StringBuilder();
          String reqUri = request.getRequestURI();
          sb.append(" ●▂● :");
          sb.append("请求路径 ：");
          sb.append(reqUri);
          sb.append(" ; 请求参数 : ");
          log.info("paramMap : [{}]",paramMap.size());
          if(paramMap != null && paramMap.size() > 0){
              paramMap.forEach((k,v) ->{
                  sb.append(" <");
                  sb.append(k);
                  sb.append(" > = ");
                  for(int i =0 ;i < v.length;i++){
                      sb.append("[");
                      sb.append(v[i]);
                  }
                  sb.append("] ;");
              });
          }

          log.info(sb.toString());
       }
    }
    @Around(value = "executeMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //log.info("zzzz");
        BindingResult bindingResult = null;
        for(Object arg:joinPoint.getArgs()){
            if(arg instanceof BindingResult){
                bindingResult = (BindingResult) arg;
            }
        }
        if(bindingResult != null){
           // log.info("bbbbb");
            List<ObjectError> errors = bindingResult.getAllErrors();
            if(errors.size()>0){
                StringBuilder msg = new StringBuilder();
                for(ObjectError error :errors){
                    msg.append(error.getDefaultMessage());
                    msg.append(",");
                }
                return  ResultData.success(msg.toString());
            }
        }
        return joinPoint.proceed();
    }
}
