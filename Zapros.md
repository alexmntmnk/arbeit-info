Примерно такой запрос с параметрами у вас должен получиться
http://localhost:8180/realms/todoapp-realm/protocol/openid-connect/auth?
response_type=code&client_id=todoapp-client&state=sidyuf8s67dfisdgf&scope=openid profile&redirect_uri=https://localhost:8080/redirect Э



тот запрос нужно обязательно выполнить в браузере (не в Postman), потому что требуется ручной ввод логин/пароль.
