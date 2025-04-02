package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderFactory {

    private final Map<String, OAuthProvider> providers;

    public OAuthProviderFactory(List<OAuthProvider> providerList) {
        this.providers = providerList.stream().collect(Collectors.toMap(
                provider -> provider.getClass().getSimpleName().replace("OAuthProvider", "").toLowerCase(),
                provider -> provider
        ));
    }

    public OAuthProvider getProvider(String providerName) {
        return providers.get(providerName.toLowerCase());
    }

}