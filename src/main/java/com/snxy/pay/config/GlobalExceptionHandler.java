package com.snxy.pay.config;

import com.snxy.common.exception.BizException;
import com.snxy.common.exception.ValidateException;
import com.snxy.common.response.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by 24398 on 2018/8/30.
 */
@Slf4j
@RestControllerAdvice
@RequestMapping( value = "/error")

public class GlobalExceptionHandler  {

  /*  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultData httpRequestMethodNotSupportedException(HttpServletResponse response) {
        //  response.setStatus(200);
        log.error("method 方法不支持");
        return ResultData.fail( "method 方法不支持");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResultData httpMediaTypeNotSupportedException(Exception e,HttpServletResponse response) {
        log.error("不支持媒体类型");
        e.printStackTrace();
        return ResultData.fail("不支持媒体类型");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultData notFoundPage404(HttpServletResponse response) {
        log.error("没有找到访问资源");
        return ResultData.fail("没有找到访问资源",404);
    }

    @RequestMapping(value = "/401" ,produces = {"application/json;charset=UTF-8"})
    public ResultData forbidden401(HttpServletResponse response) {
        log.error("没有访问权限");
        return ResultData.fail("没有访问权限",401);
    }

    @RequestMapping(value = "/403" ,produces = {"application/json;charset=UTF-8"})
    public ResultData forbidden403(HttpServletResponse response) {
        response.setStatus(200);
        log.error("没有访问权限");
        return ResultData.fail("没有访问权限",403);
    }*/

  //  @ExceptionHandler({BizException.class, ValidateException.class})
    @ExceptionHandler({Exception.class})
    public ResultData  exceptionHandler(Exception e){
        log.error("全局异常处理 ：[{}]",e);
        if(e instanceof BizException){
            BizException bizException = (BizException) e;
            String errMsg = bizException.getErrMsg();
            if(  bizException.getErrCode() != null){
                Integer  errCode = Integer.parseInt(bizException.getErrCode());
                return ResultData.fail(errMsg,errCode);
            };
            return ResultData.fail(errMsg);
        }else if(e instanceof ValidateException){
            ValidateException validateException = (ValidateException) e;
            String errMsg = validateException.getErrMsg();

            if(  validateException.getErrCode() != null){
                Integer  errCode = Integer.parseInt(validateException.getErrCode());
                return ResultData.fail(errMsg,errCode);
            };

            return ResultData.fail(errMsg);
        } else if (e instanceof ConstraintViolationException){
            Set<ConstraintViolation<?>> constraintViolations =((ConstraintViolationException) e).getConstraintViolations();
            Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
            StringBuffer errMsg=new StringBuffer();
            while (iterator.hasNext()) {
                ConstraintViolation<?> cvl = iterator.next();
                errMsg.append(cvl.getMessageTemplate());
                if(iterator.hasNext()){
                    errMsg.append(",");
                }
            }
            return ResultData.fail(errMsg.toString());
        }
       else{

        }

        return ResultData.fail(e.getMessage());
    }


}
