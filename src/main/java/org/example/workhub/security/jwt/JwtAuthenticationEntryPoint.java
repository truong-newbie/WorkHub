package org.example.workhub.security.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.example.workhub.base.RestData;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.util.BeanUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class JwtAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

  @SneakyThrows
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    MessageSource messageSource = BeanUtil.getBean(MessageSource.class);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String message = messageSource.getMessage(ErrorMessage.UNAUTHORIZED, null, LocaleContextHolder.getLocale());
    response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(RestData.error(message)));
  }

}
