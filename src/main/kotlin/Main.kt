import kotlin.random.Random


interface CurrencyPair {
    val base: String
    val quote: String
    var rate: Int
}

data class SimpleCurrencyPair(
    override val base: String,
    override val quote: String,
    override var rate: Int
) : CurrencyPair

class Wallet {
    private val balances: MutableMap<String, Int> = mutableMapOf()

    fun setBalance(currency: String, amount: Int) {
        balances[currency] = amount
    }

    fun getBalance(currency: String): Int {
        return balances.getOrDefault(currency, 0)
    }

    fun add(currency: String, amount: Int) {
        val current = getBalance(currency)
        balances[currency] = current + amount
    }

    fun subtract(currency: String, amount: Int): Boolean {
        val current = getBalance(currency)
        return if (current >= amount) {
            balances[currency] = current - amount
            true
        } else {
            false
        }
    }

    fun displayBalances() {
        println("Баланс:")
        for ((currency, amount) in balances) {
            val mainAmount = amount / 100
            println("$currency: $mainAmount")
        }
    }
}


class ExchangeTerminal {
    private val userWallet = Wallet()
    private val terminalWallet = Wallet()
    private val currencyPairs: MutableMap<String, CurrencyPair> = mutableMapOf()

    init {
        userWallet.setBalance("RUB", 1000000 * 100)

        terminalWallet.setBalance("RUB", 10000 * 100)
        terminalWallet.setBalance("USD", 1000 * 100)
        terminalWallet.setBalance("EUR", 1000 * 100)
        terminalWallet.setBalance("USDT", 1000 * 100)
        terminalWallet.setBalance("BTC", (1.5 * 100).toInt())

        addCurrencyPair("RUB", "USD", 90 * 100)
        addCurrencyPair("RUB", "EUR", 100 * 100)
        addCurrencyPair("USD", "EUR", (1.2 * 100).toInt())
        addCurrencyPair("USD", "USDT", 1 * 100)
        addCurrencyPair("USD", "BTC", 5000 * 100)
    }

    private fun addCurrencyPair(base: String, quote: String, rate: Int) {
        val pairKey = "$base/$quote"
        currencyPairs[pairKey] = SimpleCurrencyPair(base, quote, rate)
    }

    private fun updateRate(pair: CurrencyPair) {
        val changePercentage = Random.nextDouble(-0.05, 0.05)
        pair.rate = (pair.rate * (1 + changePercentage)).toInt()
    }

    private fun displayCurrencyPairs() {
        println("Доступные валютные пары и их курсы:")
        for ((key, pair) in currencyPairs) {
            val rateMain = pair.rate / 100
            println("$key: $rateMain")
        }
    }

    private fun executeExchange(from: String, to: String, amount: Int) {
        val pairKey = "$from/$to"

        val pair = currencyPairs[pairKey]
        if (pair == null) {
            println("Валютная пара $from/$to не найдена.")
            return
        }

        val rate = pair.rate

        val exchangeAmount: Int
        val userDeduct: String
        val terminalDeduct: String
        val userAdd: String
        val terminalAdd: String

        exchangeAmount = ((amount / rate) * 100).toInt()
        userDeduct = from
        userAdd = to
        terminalDeduct = to
        terminalAdd = from

        if (userWallet.getBalance(userDeduct) < amount) {
            println("Недостаточно средств у пользователя.")
            return
        }

        if (terminalWallet.getBalance(terminalDeduct) < exchangeAmount) {
            println("Недостаточно средств в терминале.")
            return
        }

        userWallet.subtract(userDeduct, amount)
        userWallet.add(userAdd, exchangeAmount)

        terminalWallet.subtract(terminalDeduct, exchangeAmount)
        terminalWallet.add(terminalAdd, amount)

        println("Обмен выполнен успешно.")

        currencyPairs.values.forEach { updateRate(it) }
    }

    fun run() {
        val greet: () -> Unit = {print("Добро пожаловать в валютный терминал обмена!")}
        greet

        while (true) {
            println("\nВыберите действие:")
            println("1. Просмотреть баланс")
            println("2. Просмотреть валютные пары")
            println("3. Сделать обмен")
            println("4. Выйти")

            print("Введите номер действия: ")
            val input = readLine()

            when (input) {
                "1" -> {
                    userWallet.displayBalances()
                }
                "2" -> {
                    displayCurrencyPairs()
                }
                "3" -> {
                    print("Введите валюту, которую хотите продать (например, RUB): ")
                    val from = readLine()?.trim()?.toUpperCase() ?: ""
                    print("Введите валюту, которую хотите купить (например, USD): ")
                    val to = readLine()?.trim()?.toUpperCase() ?: ""
                    print("Введите сумму в $from: ")
                    val amountInput = readLine()

                    val amount = try {
                        (amountInput?.replace("'", "")?.toInt()) ?: 0
                    } catch (e: NumberFormatException) {
                        println("Некорректный ввод суммы.")
                        continue
                    }

                    if (amount <= 0) {
                        println("Сумма должна быть положительной.")
                        continue
                    }

                    executeExchange(from, to, amount * 100)
                }
                "4" -> {
                    println("До свидания!")
                    break
                }
                else -> {
                    println("Некорректный выбор. Пожалуйста, попробуйте снова.")
                }
            }
        }
    }
}


fun main() {
    val terminal = ExchangeTerminal()
    terminal.run()
}
