# edison-vault
Library to access Vault servers and inject secrets into Edison services.

[![Build Status](https://travis-ci.org/otto-de/edison-vault.svg?branch=master)](https://travis-ci.org/otto-de/edison-vault)

## Usage
This library implements a Spring PropertySourcesPlaceholderConfigurer which extends and replaces the default one. It maps 
values from vault-secrets to properties which can then be easily accessed via 
<a href="http://docs.spring.io/spring/docs/4.2.1.RELEASE/javadoc-api//org/springframework/beans/factory/annotation/Value.html">@Value</a> 
annotations. To use this library the secrets must be setup like described in the <a href="#vault">vault configuration section</a>. 
 
If your vault setup matches the requirements you just need to set the configuration properties in your 
application.properties file. You can find them in the <a href="#properties">application.properties</a> configuration section.

## <a name="vault">Vault configuration</a>
In Vault the App ID authentication backend has to be enabled. In this context tuples of app-ids and user-ids have to be 
created in Vault.

Each property you want to save in vault must be created as a single secret under an individual secret path sharing the 
same prefix. The last part of the secret-path will be the property key. The property value must be saved in the 
underlying JSON-Property named "value".

Example

    GET http://yourVaultHostName:4001/v1/some/secret/path/secretOne 
    {
      "value": "theSecretNumberOne"
    }
  
    GET http://yourVaultHostName:4001/v1/some/secret/path/secretTwo 
    {
      "value": "theSecretNumberTwo"
    }

In this example you will get two properties: secretOne=theSecretNumberOne and secretTwo=theSecretNumberTwo

For further vault documentation see <a href="http://www.vaultproject.io/">http://www.vaultproject.io/</a> 

## <a name="properties">application.properties configuration</a>

- edison.vault.enabled      enable edison-vault (default=false)
- edison.vault.properties   comma-separated list of property keys to fetch from vault (default=empty).
- edison.vault.base-url     url of vault server
- edison.vault.secret-path  vault secret path  
- edison.vault.appid       app id to access the vault server
- edison.vault.userid      user id to access the vault server


## Example

application.properties:

    edison.vault.enabled=true
    edison.vault.properties=someVaultPropertyKey,someOtherVaultPropertyKey
    edison.vault.base-url=https://yourVaultHostName:8200
    edison.vault.secret-path=/some/secret/path/  
    edison.vault.appid=aaaaaaaa-bbbb-cccc-dddd-eeeeeeffffff
    edison.vault.userid=ffffffff-eeee-dddd-cccc-bbbbbbaaaaa

SomeClass.java:

    public class SomeClass {
        
        @Value("secretOne")
        private String theSecretNumberOne;

        public void someMethod(@Value("secretTwo") String theSecretNumberTwo) {

        }
    }
