### Телеграм бот для компании Папа Блинов

##### Версия на гитхабе по идее будет настроена под запуска в докере
#### Запуск (без docker-compose)
- добавить редис(можно в докере `sudo docker run --name redis -p 6379:6379 -d redis`)
- запустить ngrok `ngrok http 8092`
- добавить нужные переменные среды в удобный вам формат(см. yml-файл с конфигами)
- запустить приложение
- активировать веб-хук post-запросом на `localhost:8092/webhook/create`
- измените в классах `ProcessingSearchRequestsService` и `NotionServiceImpl` константы на верный путь к папке с локлальными ресурсами(это нужно для корректного скачивания и поиска, и отправки файлов)
- если запускаете первый раз - скачайте нужные файлы с нужных страниц notion http-post-запросом на `localhost:8092/notion/download_all_files` для корректной работы нужно указать id страниц в notion, я указывал их в файле  `page_ids.txt` в ресурсах, вы можете сделать также

#### Запуск (с docker-compose)
- поднять контейнеры `sudo docker-compose up --build -d`, проверить `sudo docker-compose ps -a`
- активировать веб-хук post-запросом на `localhost:8092/webhook/create`
- закачать нужные файлы с notion post-запросом на `localhost:8092/notion/download_all_files`

#### Реализвоано на данный момент
- полученые сообщений пользвоателей через веб хук ngrok
- обработка(маппинг) сообщений и отправка запроса в open ai
- получение ответа от ллм и отправка обратно пользователю 
- маппинг всех типов сообщений - личных и из группы. 
- фильтрация сообщений из группы по 2 триггерам - обращение к боту по нику или ответ на прошлое сообщение бота
- добавление ручек для выкачивания файлов из notion и локлаьного хранения в ресурсах (есть логика, которая фильтрует файлы по названию и скачивает только новые)
- добавление двух вариантов работы с ботом - общение с ИИ и скачивание файлов, последнее реализовано через запрос к ИИ - ИИ отдает json с точными именами файлов из векторной БД, далее происходит поиск локлаьно сохраненных файлов с таким именем и отправка юзеру
- реализованы все основные команды(старт, общая инфа, переключение на разговор и выкачку файлов, чоистка памяти ИИ, помощь) и сделана логика для команд
- хранение состояния о треде и кол-ве сообщений в диалоге реализвоано через redis, при достижении 10 сообщений в треде - происходит их резюмирование, создание нового треда с резюме и удаление старого для экономии токенов
- починил редис(был баг в коммите `7c1873bca4e9185dfaa0d02182327e8a8a41cc66`), теперь делаю инкрементацию чуть замороченнее, но она работает
- все упаковано в докер - добавлен том для хранения файлов и изменены настройки путей для скачивания\хранения и отправки файлов (в классах `ProcessingSearchRequestsService` и `NotionServiceImpl`)


Требуется подключить
- скрипт для деплоя