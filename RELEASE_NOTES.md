# Release Notes

## 0.2.0
*   added 4 cryptocurrency pairs from Bybit exchange. The full list of available cryptocurrency pairs is:
    BTC_USDT, ETH_USDT, SOL_USDT, WLKN_USDT. [Issue #4]

## 0.1.0
*   added 6 more currency pairs from Moscow Exchange (MOEX). The full list of available currency pairs is:
    USD_RUB, EUR_RUB, CNY_RUB, TRY_RUB, EUR_USD, USD_KZT, RUB_KZT; [Issue #3]
*   fixed the bug when the bot sent null quotes on weekdays approximately at 6:30-7:20 a.m. Moscow time. Now
    at this time bot sends the close prices of the previous trading day for each currency pair; [Issue #6]
*   fixed inaccuracy in the displayed time of quotes relevance: now the time at which the quotes are
    relevant is taken from MOEX and shown correctly.

## 0.0.1
*   a Telegram-bot which can send the USD_RUB quote to users.
