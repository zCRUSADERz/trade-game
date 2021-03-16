# Серверная часть (REST API) игры про торговлю
## Установка
Для запуска необходимы инструменты: [Docker engine](https://docs.docker.com/engine/install/#server) и 
[Docker compose](https://docs.docker.com/compose/install/). После установки инструментов необходимо 
скачать compose.yaml файл из проекта или проект полностью и в корне проекта выполнить команду 
```
docker compose -p trade-game up
```
## Об игре
В игре реализован функционал принятия ордеров на покупку и продажу некоторого объема товаров от 
пользователей. После создания ордера система автоматически подбирает пару(BUY - SELL) для нового 
ордера и выполняет исполнение ордеров, а именно балансовые операции и передачу товара от продавца 
к покупателю. 

Текущее состояние рынка можно оценить по [биржевому стакану](https://ru.wikipedia.org/wiki/%D0%91%D0%B8%D1%80%D0%B6%D0%B5%D0%B2%D0%BE%D0%B9_%D1%81%D1%82%D0%B0%D0%BA%D0%B0%D0%BD)

## Аутентификация и авторизация
В игре по умолчанию заведены несколько пользователей (admin, user1, user2, user3) с одинаковым 
паролем "password". Для аутентификации используется механизм Basic, поэтому HTTP запрос должен 
содержать Header "Authorization".

Пользователь admin имеет права доступа ко всему функционалу, а так же права SUPERVISOR (имеет 
право на чтение любых REST ресурсов).

Пользователь user1 имеет права SUPERVISOR

Для выполнения всех не безопасных HTTP запросов необходимо указывать Header "X-XSRF-TOKEN" его 
значение необходимо взять из Cookie "XSRF-TOKEN"

Регистрация новых пользователей еще не реализована.

REST ресурс "order" имеет ограничения по правам чтения и изменения, обычный пользователь не может 
видеть или изменять чужие ордера.

## Примеры REST запросов
1. ```curl -v -u user2:password -b cookie.txt -c cookie.txt localhost/api/``` точка входа в 
    приложение
2. ```curl -v -u user2:password localhost/api/orders``` коллекция REST ресурса "order"
3. ```
   curl -v -X POST -u user2:password -b cookie.txt -c cookie.txt \
      	-H "Content-Type: application/json" \
      	-H "X-XSRF-TOKEN: 4cb5b1e8-1d2e-4cc9-b56f-08ce754030c4" \
      	--data '{"type": "BUY", "quantity": 10, "price": 14}' \
      	localhost/api/orders
   ``` 
   POST запрос на создание нового ордера. Тип ордера (type): BUY и SELL; количество (quantity) - 
   целочисленное; цена (price) - NUMBER(18, 2)
4. ```curl -v -u user2:password -b cookie.txt -c cookie.txt localhost/api/orders/1``` Возвращает  
    ордер с id 1. Обычный пользователь не может просматривать чужие ордера, поэтому можно получить 
    код ответа "403 Forbidden".    
    Изменение ордера пользователем посредством PUT POST PATCH DELETE запросов запрещено.
5. ``` 
   curl -v -X POST -u user2:password -b cookie.txt -c cookie.txt \
        -H "X-XSRF-TOKEN: 4cb5b1e8-1d2e-4cc9-b56f-08ce754030c4" \
        localhost/api/orders/1/cancel
   ``` 
   POST запрос для отмены исполнения ордера с id 1.
6. ```curl -v -u user2:password -b cookie.txt -c cookie.txt localhost/api/depthOfMarket```
   Возвращает ресурс представляющий биржевой стакан. Записи сгруппированы по цене и типу ордера, 
   для каждого ценового уровня представлена информация о количестве активных ордеров и общем 
   оставшемся объеме по этим ордерам.
7. ```
    curl -v -X POST -u user2:password -b cookie.txt -c cookie.txt \
         -H "Content-Type: application/json" \
         -H "X-XSRF-TOKEN: 4cb5b1e8-1d2e-4cc9-b56f-08ce754030c4" \
         --data '{ "randomType": true,"minPrice": 10,"maxPrice": 100,"maxQuantity": 50,"minQuantity": 1,"numberOfBatches": 50,"batchSize": 100,"delayBetweenBatches": 50}' \
         localhost/api/orders/createOrders
   ```
   Пакетное создание ордеров, разрабатывалось для нагрузочного тестирования. Доступ к данной 
   операции имеют пользователи (admin, user1, user2, user3).    
   ```randomType``` - тип ордера будет выбираться рандомно, если ```"randomType": false```, то 
   необходимо указать ```"orderType": "BUY" или "SELL"```, в этом случае будут создаваться ордера 
   одного типа.   
   ```delayBetweenBatches``` - задержка в миллисекундах до создания следующего пакета ордеров.
   
   В данном проекте я делал упор на оптимизацию и максимальную производительность исполнения 
   ордеров. Все тяжелые SQL запросы связанные с исполнением были оптимизированы.
8. ```
   curl -v -X POST -u user2:password -b cookie.txt -c cookie.txt \
        -H "X-XSRF-TOKEN: 4cb5b1e8-1d2e-4cc9-b56f-08ce754030c4" \
        localhost/api/orders/sendToExecution?limit=100
   ```
   Ручная отправка ордеров на исполнение. Отбираются только не исполненные, не отмененные, не 
   находящиеся на исполнении ордера и для которых есть пары. Разрабатывалось с целью 
   администрирования и устранения проблем с исполнением ордеров. Доступ к данной операции имеют 
   пользователи с ролью "ADMIN" (пользователь admin)
9. ```
   curl -v -X POST -u user2:password -b cookie.txt -c cookie.txt \
   	    -H "X-XSRF-TOKEN: 4cb5b1e8-1d2e-4cc9-b56f-08ce754030c4" \
   	    localhost/api/orders/addWorkersForOrderExecution?workers=1
   ```
   Ручной запуск потока для исполнения ордеров. Доступ к данной операции имеют пользователи с 
   ролью "ADMIN". По умолчанию запускается количество потоков исполнения ордеров равное числу ЦПУ. 
   Это можно изменить поправив файл compose.yaml, необходимо для сервиса "app" к "command" 
   добавить следующий аргумент --ru.yakovlev.order.execution.workers=0