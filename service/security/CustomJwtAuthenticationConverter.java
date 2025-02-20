package org.tpl.chat.service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class CustomJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    return new JwtAuthenticationToken(
        source,
        Stream.concat(
                new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                extractAuthorities(source).stream())
            .collect(toSet()));
  }

  private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> authorities = jwt.getClaim("authorities");
    return CollectionUtils.isEmpty(authorities)
        ? emptySet()
        : authorities.stream().map(SimpleGrantedAuthority::new).collect(toSet());
  }
}
