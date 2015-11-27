package de.otto.edison.vault;

public class VaultTokenFactory {
    public VaultToken createVaultToken(String vaultBaseUrl) {
        return new VaultToken(vaultBaseUrl);
    }
}
