#!/bin/bash

# Путь к проекту на локальной машине
PROJECT_PATH=~/proj/TelegramBotForPapaBlinov

# Данные для подключения к удаленной машине
REMOTE_USER=root
REMOTE_IP=94.241.172.33
REMOTE_PASS="p.gc5zYD36*y5s"

# Путь на удаленной машине
REMOTE_PROJECT_PATH=/root/telegram_bots/TelegramBotForPapaBlinov

if ! command -v sshpass &> /dev/null; then
    echo "sshpass не установлен. Пожалуйста, установите sshpass и попробуйте снова."
    exit 1
fi

echo "Копирование проекта на удаленную машину..."
sshpass -p $REMOTE_PASS scp -r $PROJECT_PATH $REMOTE_USER@$REMOTE_IP:$REMOTE_PROJECT_PATH

echo "Перезапуск контейнеров на удаленной машине..."
sshpass -p $REMOTE_PASS ssh $REMOTE_USER@$REMOTE_IP << EOF
echo "Переход в директорию проекта..."
cd $REMOTE_PROJECT_PATH || { echo "Ошибка: Не удалось перейти в директорию $REMOTE_PROJECT_PATH"; exit 1; }

echo "Остановка старых контейнеров..."
sudo docker-compose down || echo "Не удалось остановить старые контейнеры."

echo "Запуск новых контейнеров..."
sudo docker-compose up --build -d || echo "Не удалось запустить контейнеры."

echo "Очистка неиспользуемых ресурсов Docker..."
sudo docker container prune -f
sudo docker image prune -a -f
sudo docker network prune -f
sudo docker volume prune -f
exit
EOF