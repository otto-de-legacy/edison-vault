# edison-vault
Library to access Vault servers and inject secrets into Spring-Boot applications.

[![Build Status](https://travis-ci.org/otto-de/edison-vault.svg?branch=master)](https://travis-ci.org/otto-de/edison-vault) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-vault/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-vault)

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

For further vault documentation see <a href="http://www.vaultproject.io/">http://www.vaultproject.io/</a>

## <a name="mapping">Spring property mapping</a>

All properties you want to load from vault must be located under the same parent path. You must configure the parent path by
setting the configuration property **edison.vault.secret-path**

After loading all properties (e.g. from application.properties), the VaultPropertyPlaceholderConfigurer scans the values.
If a value starts with **vault://** it is considered as a vault property which has to be read from vault.


Example

    application.properties:
        ...
        edison.vault.secret-path=/my/secret/path/
        ...
    
        foo.bar.secret1=vault://my/secret#value
        # Vault-Call:
        # GET http://yourVaultHostName:4001/v1/my/secret/path/my/secret/
        # {
        #    "value": "theFirstSecretValueYouWant"
        # }
    
        foo.bar.secret2=vault://my.secret#value
        # Vault-Call:
        # GET http://yourVaultHostName:4001/v1/my/secret/path/my.secret/
        # {
        #    "value": "theSecondSecretValueYouWant"
        # }
    
        foo.bar.secret3=vault://my/secret#othervalue
        # Vault-Call:
        # GET http://yourVaultHostName:4001/v1/my/secret/path/my/secret/
        # {
        #    "othervalue": "theThirdSecretValueYouWant"
        # }
        
        foo.bar.secret4=vault://#value
        # Vault-Call:
        # GET http://yourVaultHostName:4001/v1/my/secret/path/
        # {
        #    "value": "theFourthSecretValueYouWant"
        # }


In this example you will get three spring properties with the following values:

- foo.bar.secret1=theFirstSecretValueYouWant
- foo.bar.secret2=theSecondSecretValueYouWant
- foo.bar.secret3=theThirdSecretValueYouWant
- foo.bar.secret4=theFourthSecretValueYouWant

You see how the parent secret-path is used and how a spring property key is mapped to a vault path.
The part before the '#' is considered the path of the secret, the part after the '#' is the json-key of the
value you want to load.

## <a name="properties">application.properties configuration</a>

- edison.vault.enabled              enable edison-vault (default=false)
- edison.vault.base-url             url of vault server
- edison.vault.secret-path          vault secret path  
- edison.vault.token-source         how to access the vault server token -- possible values are login,file or environment
- edison.vault.appid                app id to access the vault server (valid for token-source=login)
- edison.vault.userid               user id to access the vault server (valid for token-source=login)
- edison.vault.environment-token    environment-variable which holds the token (valid for token-source=environment)
- edison.vault.file-token           filename where the token is stored in, if not set then $HOME/.vault-token is used  (valid for token-source=file)

## Example

application.properties:

    edison.vault.enabled=true
    edison.vault.base-url=https://yourVaultHostName:8200
    edison.vault.secret-path=/my/secret/path/
    edison.vault.token-source=login
    edison.vault.appid=aaaaaaaa-bbbb-cccc-dddd-eeeeeeffffff
    edison.vault.userid=ffffffff-eeee-dddd-cccc-bbbbbbaaaaa

SomeClass.java:

    public class SomeClass {
        
        @Value("${foo.bar.secret1}")
        private String theSecretNumberOne;

        public void someMethod(@Value("${foo.bar.secret2}") String theSecretNumberTwo) {

        }
    }
