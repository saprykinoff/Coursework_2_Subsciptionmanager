### !!История коммитов пропала при слиянии с main.
Сами комиты находятся в ветке `subscribeManager`

## Подписочный бот
Так как подписочные сервисы по типу Boosty набирают популряность было бы удобно иметь аналог этих сервисом прямо в телеграме.
Поэтому был разработан бот, имплементирующий данное поведение

## Как пользоваться

### Администратору

Для создания группы выполните следующие шаги: 
1) Создайте группу в телеграмме 
2) Добавьте этого бота в группу и назначте его администратором (Ban users, Invite users via link) 
3) Используйте команду <code>/reg name [price = 100 [duration =30]]</code>  
чтобы зарегестрировать группу в системе, где <code>name</code> это уникальное имя вашей группы,  
<code>price</code> это стоимость в рублях за <code>duration</code> дней 
4) Сообщите ваше уникальное имя своим подписчикам, чтобы они смогли подписываться

### Подписчику

1) Для того чтобы оплатить подписку используйте команду <code>/sub name</code>,  
где <code>name</code> это уникальное имя группы на которую вы хотите подписаться 
2) Оплатите полученный QR-code используя сайт https://pay.raif.ru/pay/rfuture/#/reader 
3) После оплаты вы получите уникальную ссылку на группу. Вы можете как воспользоваться ей сами, так  
и отдать ее кому-то. Важно: Подписка уже активна, даже если никто еще не вступил по ссылке 
4) Перейдя по ссылке вы автоматически будете добавлены в группу 
5) Посмотреть активные подписки можно используя команду `/list` 
6) В случае необходимости воспользуйтесь командой <code>/unsub index</code> чтобы аннулировать подписку под номером `index`.  
7) Чтобы узнать номер воспользуйтесь коммандой `/list` (цифра в скобках и есть необходимый номер) 