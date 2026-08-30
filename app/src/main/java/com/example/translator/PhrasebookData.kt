package com.example.translator

data class PhraseItem(
    val id: String,
    val banglaText: String,
    val banglaPronunciation: String,
    val englishText: String,
    val category: String,
    val tag: String
)

data class EarbudTip(
    val title: String,
    val description: String,
    val iconName: String
)

object PhrasebookData {

    val categories = listOf(
        "All",
        "Essentials",
        "Travel & Transport",
        "Shopping & Money",
        "Dining & Food",
        "Emergency"
    )

    val phrases = listOf(
        // Essentials
        PhraseItem("ess_1", "শুভ সকাল", "Shuvo shokal", "Good morning", "Essentials", "greetings"),
        PhraseItem("ess_2", "আপনি কেমন আছেন?", "Apni kemon achen?", "How are you?", "Essentials", "greetings"),
        PhraseItem("ess_3", "আমি ভালো আছি, ধন্যবাদ।", "Ami bhalo achi, dhonnobad", "I am doing well, thank you.", "Essentials", "greetings"),
        PhraseItem("ess_4", "আপনার নাম কী?", "Apnar naam ki?", "What is your name?", "Essentials", "greetings"),
        PhraseItem("ess_5", "আমার নাম...", "Amar naam...", "My name is...", "Essentials", "greetings"),
        PhraseItem("ess_6", "আপনাকে অনেক ধন্যবাদ।", "Apnake onek dhonnobad", "Thank you very much.", "Essentials", "polite"),
        PhraseItem("ess_7", "মাফ করবেন / শুনছেন?", "Maaf korben / shunchen?", "Excuse me / Pardon me?", "Essentials", "polite"),
        PhraseItem("ess_8", "আমি বুঝতে পারছি না।", "Ami bujhte parchi na", "I do not understand.", "Essentials", "polite"),
        PhraseItem("ess_9", "দয়া করে একটু ধীরে কথা বলুন।", "Doya kore ektu dhire kotha bolun", "Please speak a bit slower.", "Essentials", "polite"),
        PhraseItem("ess_10", "আবার দেখা হবে!", "Abar dekha hobe!", "See you again soon!", "Essentials", "greetings"),

        // Travel & Transport
        PhraseItem("trv_1", "নিকটতম বিমানবন্দর/এয়ারপোর্ট কোথায়?", "Nikottomo airport kothay?", "Where is the nearest airport?", "Travel & Transport", "directions"),
        PhraseItem("trv_2", "ট্রেন স্টেশন বা বাস টার্মিনাল কোথায়?", "Train station ba bus terminal kothay?", "Where is the train or bus terminal?", "Travel & Transport", "directions"),
        PhraseItem("trv_3", "আমাকে এই ঠিকানায় নিয়ে যান।", "Amake ei thikanay niye jan", "Please take me to this address.", "Travel & Transport", "taxi"),
        PhraseItem("trv_4", "এখানে যেতে কত সময় লাগবে?", "Ekhane jete koto shomoy lagbe?", "How long will it take to get here?", "Travel & Transport", "taxi"),
        PhraseItem("trv_5", "এখানে থামুন প্লিজ।", "Ekhane thamun please", "Please stop right here.", "Travel & Transport", "taxi"),
        PhraseItem("trv_6", "সোজা এগিয়ে ডান দিকে ঘুরুন।", "Shoja egiye daan dike ghurun", "Go straight and turn right.", "Travel & Transport", "directions"),
        PhraseItem("trv_7", "নিকটতম হোটেল কোথায়?", "Nikottomo hotel kothay?", "Where is the nearest hotel?", "Travel & Transport", "hotel"),

        // Shopping & Money
        PhraseItem("shp_1", "এটার দাম কত?", "Etar daam koto?", "How much does this cost?", "Shopping & Money", "price"),
        PhraseItem("shp_2", "এটা খুব দামি, একটু কমানো যাবে?", "Eta khub daami, ektu komano jabe?", "It is too expensive, can you give a discount?", "Shopping & Money", "bargain"),
        PhraseItem("shp_3", "আমি কি ক্রেডিট কার্ড দিয়ে দিতে পারি?", "Ami ki credit card diye dite pari?", "Can I pay with a credit card?", "Shopping & Money", "payment"),
        PhraseItem("shp_4", "আমার একটি ক্যাশ রসিদ দরকার।", "Amar ekti cash roshid dorkar", "I need a purchase receipt.", "Shopping & Money", "payment"),
        PhraseItem("shp_5", "আপনার কাছে অন্য কোনো রঙ বা সাইজ আছে?", "Apnar kache onno rong ba size ache?", "Do you have another color or size?", "Shopping & Money", "product"),

        // Dining & Food
        PhraseItem("din_1", "একটু খাবার পানি দিন দয়া করে।", "Ektu khabar pani din doya kore", "Please give me some drinking water.", "Dining & Food", "order"),
        PhraseItem("din_2", "মেনুটি দেখতে পারি?", "Menuti dekhte pari?", "Could I see the food menu?", "Dining & Food", "order"),
        PhraseItem("din_3", "আপনার রেস্তোরাঁর সেরা খাবার কোনটি?", "Apnar restora-r shera khabar konti?", "What is your restaurant's specialty?", "Dining & Food", "recommend"),
        PhraseItem("din_4", "খাবারটি খুব চমৎকার ও সুস্বাদু ছিল!", "Khabarti khub chomokkar o shushadhu chilo!", "The meal was wonderful and delicious!", "Dining & Food", "compliment"),
        PhraseItem("din_5", "বিলটি নিয়ে আসুন প্লিজ।", "Bilti niye ashun please", "Could you bring the bill please?", "Dining & Food", "payment"),

        // Emergency
        PhraseItem("emg_1", "আমাকে জরুরি সাহায্য করুন!", "Amake joruri shahajjo korun!", "Please help me, it is an emergency!", "Emergency", "urgent"),
        PhraseItem("emg_2", "আমার একজন ডাক্তার প্রয়োজন।", "Amar ekjon doctor proyojon", "I need a medical doctor.", "Emergency", "medical"),
        PhraseItem("emg_3", "পুলিশ ডাকুন দয়া করে!", "Police daakun doya kore!", "Please call the police immediately!", "Emergency", "police"),
        PhraseItem("emg_4", "আমি আমার পাসপোর্ট ও ব্যাগ হারিয়ে ফেলেছি।", "Ami amar passport o bag hariye felechi", "I have lost my passport and bag.", "Emergency", "lost"),
        PhraseItem("emg_5", "হাসপাতালটি কোন দিকে?", "Hospital-ti kon dike?", "Which way is the hospital?", "Emergency", "medical")
    )

    val earbudTips = listOf(
        EarbudTip(
            title = "Universal Bluetooth Earbud Support",
            description = "Works seamlessly with HOCO EQ34 Plus, AirPods, Lenovo, Realme, boAt, or any Bluetooth TWS earbuds.",
            iconName = "headphones"
        ),
        EarbudTip(
            title = "Split-Screen Face-to-Face Mode",
            description = "Place your phone on the table: The top inverted half is for the English speaker, and the bottom half is for you in Bangla!",
            iconName = "screen_rotation"
        ),
        EarbudTip(
            title = "Dual Earbud Audio Routing",
            description = "You hear natural Bangla translations directly in your earbud, while English audio plays out loud through the phone speaker for your partner.",
            iconName = "volume_up"
        ),
        EarbudTip(
            title = "Zero-Cost & Offline Ready",
            description = "Uses on-device offline speech recognition, neural phrase translation, and offline Text-To-Speech with zero data cost!",
            iconName = "offline_bolt"
        )
    )
}
