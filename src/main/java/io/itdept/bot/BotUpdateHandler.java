package io.itdept.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotUpdateHandler {

    private final ITDeptBot bot;

    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            String reply = switch (text) {
                case "/start" -> "Добро пожаловать в бот IT-отдела!";
                default -> "Я тебя не понял. Напиши /start.";
            };

            try {
                bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(reply)
                    .build());
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения", e);
            }
        }
    }
}
