package com.notfound.product.adapter.in.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gateway가 전달한 인증 정보를 컨트롤러 파라미터에 주입하는 어노테이션.
 *
 * 사용 예:
 *   public ResponseEntity<?> register(@AuthUser AuthenticatedUser user)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthUser {
}
