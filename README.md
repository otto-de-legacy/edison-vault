# edison-vault
Library to access Vault servers and inject secrets into Edison services.

[![Build Status](https://travis-ci.org/otto-de/edison-vault.svg?branch=master)](https://travis-ci.org/otto-de/edison-vault) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-vault/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-vault)

## Usage
This library implements a Spring PropertySource and appends it to the end of the existing PropertySource list. It maps 
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

All properties you want to save in vault must be located under the same parent path. You can configure the parent path by
setting the configuration property **edison.vault.secret-path**

Each spring property you want to load from vault has to be added to the configuration property **edison.vault.properties**.

An individual spring property is mapped to a vault path by the following scheme:

1) Every dot (".") is replaced by a slash ("/").
2) The part before the last slash is the sub-path of the property and has to exist in vault.
3) The part after the last slash is the json field name of the vault value.


Example

    application.properties:
        ...
        edison.vault.secret-path=/my/secret/path/
        edison.vault.properties=my-secret-value,my.secret.value,my.secret.othervalue
        ...
    
    "my-secret-value" is mapped to:
    GET http://yourVaultHostName:4001/v1/my/secret/path
    {
      "my-secret-value": "theFirstSecretValueYouWant"
    }
    
    "my.secret.value" is mapped to:
    GET http://yourVaultHostName:4001/v1/my/secret/path/my/secret/
    {
        "value": "theSecondSecretValueYouWant"
    }
    
    "my.secret.othervalue" is mapped to:
    GET http://yourVaultHostName:4001/v1/my/secret/path/my/secret/
    {
        "othervalue": "theThirdSecretValueYouWant"
    }


In this example you will get three spring properties with the following values:

- my-secret-value=theFirstSecretValueYouWant
- my.secret.value=theSecondSecretValueYouWant
- my.secret.othervalue=theThirdSecretValueYouWant

You see how the parent secret-path is used and how a spring property key is mapped to a vault path.
Notice the difference between *my-secret-value* and *my.secret.value*.

## <a name="properties">application.properties configuration</a>

- edison.vault.enabled              enable edison-vault (default=false)
- edison.vault.base-url             url of vault server
- edison.vault.secret-path          vault secret path  
- edison.vault.properties           comma-separated list of property keys to fetch from vault (default=empty).
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
    edison.vault.properties=secretOne@key1,secretOne@key2,secretTwo,secretOne
    edison.vault.token-source=login
    edison.vault.appid=aaaaaaaa-bbbb-cccc-dddd-eeeeeeffffff
    edison.vault.userid=ffffffff-eeee-dddd-cccc-bbbbbbaaaaa

SomeClass.java:

    public class SomeClass {
        
        @Value("${secretOne@key1}")
        private String theSecretNumberOne;

        public void someMethod(@Value("${secretTwo}") String theSecretNumberTwo) {

        }
    }
