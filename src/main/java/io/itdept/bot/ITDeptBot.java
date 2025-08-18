package io.itdept.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ITDeptBot extends TelegramLongPollingBot {

    private final String ADMIN_CHAT_ID = "366722822";

    @Override
    public String getBotUsername() {
        return "ase_it_bot";
    }

    @Override
    public String getBotToken() {
        return "7860811516:AAErBlfuPiy1BGHor453iODReiuSizWrUZc";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleText(update);
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке update: ", e);
        }
    }

    private void handleText(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String text = update.getMessage().getText().trim();

        switch (text) {
            case "/start", "/help" -> {
                sendMessage(chatId, """
                        Привет! Я бот IT-отдела.

                        Ниже — доступные функции:
                        """, getMainKeyboard());
            }
            case "/status" -> sendMessage(chatId, "✅ Бот на связи");
            case "/menu" -> sendMenuButton(chatId);
            case "/race_monitoring" -> sendMonitoringButton(chatId);
            case "/jenkins" -> sendMessage(chatId, "✅ Все последние билды выполнены успешно");
            case "/dhaka" -> sendMessage(chatId, "Здесь скоро будет основная и важная инфа, если ты в офисе в Дакке");
            case "/wifi" -> sendMessage(chatId, """
                    Сети Wi‑Fi:

                    📶 ASE‑VIP        – asebd‑1984
                    📶 ASE‑site       – 12345678
                    📶 ASE10          – !asebd‑3423!
                    📶 ASE1           – !asebd‑2015!
                    📶 ASE‑Internet   – asebd‑2019
                    """);
            case "/run_jobs" -> {
                if (!chatId.equals(ADMIN_CHAT_ID)) sendMessage(chatId, "⛔ Недостаточно прав.");
                else runJobs(chatId);
            }
            case "/status_docker" -> {
                if (!chatId.equals(ADMIN_CHAT_ID)) sendMessage(chatId, "⛔ Недостаточно прав.");
                else sendMessage(chatId, getServiceStatus("com.docker.service"));
            }
            default -> sendMessage(chatId, "Неизвестная команда. Напиши /help.");
        }
    }

    private void handleCallback(Update update) throws TelegramApiException {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String data = update.getCallbackQuery().getData();

        switch (data) {
            case "/status" -> sendMessage(chatId, "✅ Бот на связи");
            case "/wifi" -> sendMessage(chatId, """
                    Сети Wi-Fi:

                        📶 ASE-VIP
                        🔑 asebd-1984

                        📶 ASE-site
                        🔑 asebd-2029

                        📶 ASE10
                        🔑 !asebd-3423!

                        📶 ASE1
                        🔑 !asebd-2015!

                        📶 ASE-Internet
                        🔑 asebd-2019

                        Не делитесь с посторонними.
                    """);
            case "/dhaka" -> sendMessage(chatId, "Здесь скоро будет основная и важная инфа, если ты в офисе в Дакке");
            case "/jenkins" -> sendMessage(chatId, "✅ Все последние билды выполнены успешно");
            case "/run_jobs" -> {
                if (!chatId.equals(ADMIN_CHAT_ID)) sendMessage(chatId, "⛔ Недостаточно прав.");
                else runJobs(chatId);
            }
            case "/status_docker" -> {
                if (!chatId.equals(ADMIN_CHAT_ID)) sendMessage(chatId, "⛔ Недостаточно прав.");
                else sendMessage(chatId, getServiceStatus("com.docker.service"));
            }
            default -> sendMessage(chatId, "⚠️ Неизвестная кнопка.");
        }
    }

    private InlineKeyboardMarkup getMainKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(InlineKeyboardButton.builder().text("🤖 Статус бота").callbackData("/status").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("📶 Wi‑Fi пароли").callbackData("/wifi").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("📍 Инфо по Дакке").callbackData("/dhaka").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("🛠️ Статус Jenkins билдов").callbackData("/jenkins").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("💾 Запустить бэкап конфигов свитчей").callbackData("/run_jobs").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("🐳 Статус Docker").callbackData("/status_docker").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("📋 Че в столовке").url("http://62.84.126.106:8081/menu/").build()));
        rows.add(List.of(InlineKeyboardButton.builder().text("📊 Race monitoring").url("http://mrtg.race.net.bd:2225/cacti/graph_view.php").build()));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private void runJobs(String chatId) {
        sendMessage(chatId, "🚀 Запускаю оба батника…");
        runBatch("C:\\docker\\ansible\\switch_backup.bat");
        runBatch("C:\\docker\\ansible\\telegram_notify_delayed.bat");
    }

    private void runBatch(String path) {
        try {
            new ProcessBuilder("cmd.exe", "/c", path).inheritIO().start();
        } catch (IOException e) {
            log.error("Ошибка запуска батника: " + path, e);
            sendMessage(ADMIN_CHAT_ID, "❌ Не удалось запустить «" + path + "»\n" + e.getMessage());
        }
    }

    private record ExecResult(int code, String out) {}

    private ExecResult exec(String cmd) {
        try {
            Process p = new ProcessBuilder("cmd.exe", "/c", cmd).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes());
            int code = p.waitFor();
            return new ExecResult(code, out.trim());
        } catch (Exception e) {
            return new ExecResult(-1, e.getMessage());
        }
    }

    private String getServiceStatus(String serviceName) {
        ExecResult r = exec("sc query " + serviceName);
        if (r.code() != 0) return "❌ Ошибка: " + r.out();
        if (r.out().contains("RUNNING")) return "✅ Служба «" + serviceName + "» запущена.";
        if (r.out().contains("STOPPED")) return "⛔ Служба «" + serviceName + "» остановлена.";
        return "ℹ️ Непонятный статус:\n```\n" + r.out() + "\n```";
    }

    private void sendMenuButton(String chatId) {
        var button = InlineKeyboardButton.builder()
                .text("📋 Открыть меню")
                .url("http://62.84.126.106:8081/menu/")
                .build();

        var markup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(button)))
                .build();

        var message = SendMessage.builder()
                .chatId(chatId)
                .text("Вот меню на сегодня:")
                .replyMarkup(markup)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки кнопки меню", e);
        }
    }

    private void sendMonitoringButton(String chatId) {
        var button = InlineKeyboardButton.builder()
                .text("📊 Open Race Monitoring")
                .url("http://mrtg.race.net.bd:2225/cacti/graph_view.php")
                .build();

        var markup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(button)))
                .build();

        var message = SendMessage.builder()
                .chatId(chatId)
                .text("Мониторинг:")
                .replyMarkup(markup)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки кнопки", e);
        }
    }

    private void sendMessage(String chatId, String text, InlineKeyboardMarkup markup) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(markup).build());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения с кнопками", e);
        }
    }

    private void sendMessage(String chatId, String text) {
    try {
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(getPersistentKeyboard())
                .build());
    } catch (TelegramApiException e) {
        log.error("Ошибка отправки сообщения", e);
    }
}


    private ReplyKeyboardMarkup getPersistentKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add(new KeyboardButton("/start"));
    row1.add(new KeyboardButton("/help"));
    

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1))
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build();
}

}
