package com.example.data

import androidx.compose.ui.graphics.Color

data class FeaturedRestData(
    val name: String,
    val emoji: String,
    val offer: String,
    val time: String,
    val rating: String,
    val reviews: String,
    val distance: String,
    val cuisines: String,
    val costForTwo: String,
    val gradientColors: List<Color>,
    val id: String = ""
)

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val etaMin: Int,
    val deliveryFee: Double,
    val avgCost: Double,
    val isDroneEligible: Boolean = true,
    val promoBadge: String? = null,
    val gradientIndex: Int,
    val imageUrl: String? = null   // from backend image_url field
)

data class Dish(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isVeg: Boolean,
    val isBestSeller: Boolean = false,
    val restaurantId: String,
    val imageUrl: String? = null,  // from backend image_url field
    val weightGrams: Int = 300     // from backend weight_grams — used for drone payload calc
)

object SampleData {
    val featuredRestaurants = listOf(
        FeaturedRestData(
            name = "Ak Bakers",
            emoji = "🎂🌹✨",
            offer = "🔥 60% off upto ₹120",
            time = "45-50 MINS",
            rating = "4.6",
            reviews = "(11)",
            distance = "Auto Nagar, 0.3 km",
            cuisines = "Bakery, Indian",
            costForTwo = "₹300 for two",
            gradientColors = listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))
        ),
        FeaturedRestData(
            name = "Bhimas Indian Kitchen",
            emoji = "🥘🌶️🍛",
            offer = "★ FLAT 50% OFF",
            time = "25-30 MINS",
            rating = "4.5",
            reviews = "(120+)",
            distance = "SRM AP Hostel Road, 0.8 km",
            cuisines = "North Indian, Biryani",
            costForTwo = "₹250 for two",
            gradientColors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
        ),
        FeaturedRestData(
            name = "The Pizza Palace",
            emoji = "🍕🧀🥤",
            offer = "🎁 FREE Garlic Bread",
            time = "30-35 MINS",
            rating = "4.7",
            reviews = "(85)",
            distance = "University Plaza, 1.2 km",
            cuisines = "Pizzas, Italian, Fast Food",
            costForTwo = "₹400 for two",
            gradientColors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
        ),
        FeaturedRestData(
            name = "Sri Venkateswara Sweets",
            emoji = "🍬🍯🥛",
            offer = "🔥 Buy 1 Get 1 Free",
            time = "15-20 MINS",
            rating = "4.4",
            reviews = "(250+)",
            distance = "AP Junction, 2.0 km",
            cuisines = "Sweets, South Indian",
            costForTwo = "₹150 for two",
            gradientColors = listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
        )
    )

    val restaurants = listOf(
        Restaurant(
            id = "res-spice",
            name = "Spice Garden",
            cuisine = "Biryani & Curries",
            rating = 4.8,
            etaMin = 7,
            deliveryFee = 18.0, // ₹18, let's keep it close to ₹18-30
            avgCost = 180.0,
            promoBadge = "30% OFF above ₹299",
            gradientIndex = 0
        ),
        Restaurant(
            id = "res-burger",
            name = "The Burger Lab",
            cuisine = "Burgers & Wraps",
            rating = 4.6,
            etaMin = 9,
            deliveryFee = 22.0,
            avgCost = 220.0,
            promoBadge = "⚡ Express Drone: 9 min",
            gradientIndex = 1
        ),
        Restaurant(
            id = "res-nimbus",
            name = "Café Nimbus",
            cuisine = "Coffee & Snacks",
            rating = 4.9,
            etaMin = 5,
            deliveryFee = 15.0,
            avgCost = 150.0,
            promoBadge = "🚁 Free delivery on First order",
            gradientIndex = 2
        ),
        Restaurant(
            id = "res-dragon",
            name = "Dragon Noodles",
            cuisine = "Chinese & Thai",
            rating = 4.5,
            etaMin = 11,
            deliveryFee = 30.0,
            avgCost = 200.0,
            promoBadge = "🔥 Trending Near You",
            gradientIndex = 3
        ),
        Restaurant(
            id = "res-bakers",
            name = "Ak Bakers",
            cuisine = "Bakery, Indian",
            rating = 4.6,
            etaMin = 45,
            deliveryFee = 20.0,
            avgCost = 300.0,
            promoBadge = "🔥 60% off upto ₹120",
            gradientIndex = 4
        ),
        Restaurant(
            id = "res-bhimas",
            name = "Bhimas Indian Kitchen",
            cuisine = "North Indian, Biryani",
            rating = 4.5,
            etaMin = 25,
            deliveryFee = 25.0,
            avgCost = 250.0,
            promoBadge = "★ FLAT 50% OFF",
            gradientIndex = 5
        ),
        Restaurant(
            id = "res-pizza",
            name = "The Pizza Palace",
            cuisine = "Pizzas, Italian, Fast Food",
            rating = 4.7,
            etaMin = 30,
            deliveryFee = 18.0,
            avgCost = 400.0,
            promoBadge = "🎁 FREE Garlic Bread",
            gradientIndex = 6
        ),
        Restaurant(
            id = "res-sweets",
            name = "Sri Venkateswara Sweets",
            cuisine = "Sweets, South Indian",
            rating = 4.4,
            etaMin = 15,
            deliveryFee = 30.0,
            avgCost = 150.0,
            promoBadge = "🔥 Buy 1 Get 1 Free",
            gradientIndex = 7
        )
    )

    val dishes = listOf(
        // Spice Garden
        Dish("dish-sg-1", "Chicken Dum Biryani", "Aromatic long-grain basmati rice layered with spiced tender chicken, cooked in authentic dum style.", 189.0, isVeg = false, isBestSeller = true, restaurantId = "res-spice"),
        Dish("dish-sg-2", "Paneer Tikka Masala", "Grilled cottage cheese cubes simmered in a rich, creamy tomato gravy with traditional spices.", 149.0, isVeg = true, restaurantId = "res-spice"),
        Dish("dish-sg-3", "Butter Naan (1pc)", "Soft, leavened flatbread brushed with abundant melted butter directly from the tandoor oven.", 40.0, isVeg = true, restaurantId = "res-spice"),
        
        // Burger Lab
        Dish("dish-bl-1", "Loaded Cheese Fries", "Crispy golden dynamic french fries smothered in melted cheddar cheese sauce, jalapenos, and herbs.", 99.0, isVeg = true, isBestSeller = true, restaurantId = "res-burger"),
        Dish("dish-bl-2", "Smoky BBQ Burger", "Flame-grilled succulent patty with house BBQ reduction, dynamic crunchy onion rings, and secret laboratory sauce.", 159.0, isVeg = false, isBestSeller = true, restaurantId = "res-burger"),
        Dish("dish-bl-3", "Crispy Aloo Wrap", "Spiced golden potato patty wrap with crunchy greens, tomatoes, and dynamic tangy tandoori mayo.", 89.0, isVeg = true, restaurantId = "res-burger"),
        
        // Café Nimbus
        Dish("dish-cn-1", "Cold Brew Coffee", "Our single-origin 18-hour slow steeped specialty cold brew coffee served on pure dynamic ice blocks.", 129.0, isVeg = true, isBestSeller = true, restaurantId = "res-nimbus"),
        Dish("dish-cn-2", "Nutella Hazelnut Waffle", "Freshly baked extra-crispy golden waffle topped with rich pure Nutella drizzle and toasted crushed hazelnuts.", 139.0, isVeg = true, isBestSeller = true, restaurantId = "res-nimbus"),
        Dish("dish-cn-3", "Dynamic Blueberry Muffin", "Moist, fresh-baked wild blueberry muffin topped with sugar streusel crumble.", 79.0, isVeg = true, restaurantId = "res-nimbus"),
        
        // Dragon Noodles
        Dish("dish-dn-1", "Veg Momos (6pc)", "Steamed delicate Himalayan-style vegetable dumplings served with authentic hot & fire red chili chutney.", 89.0, isVeg = true, isBestSeller = false, restaurantId = "res-dragon"),
        Dish("dish-dn-2", "Schezwan Hakka Noodles", "Wok-tossed noodles in explosive schezwan pepper oil with crispy colorful dynamic garden vegetables.", 129.0, isVeg = true, isBestSeller = true, restaurantId = "res-dragon"),
        Dish("dish-dn-3", "Kung Pao Chicken", "Classic sweet-spicy stir fried chicken cubes with dry chilies, scallions, and roasted dynamic peanuts.", 169.0, isVeg = false, restaurantId = "res-dragon"),

        // Ak Bakers (res-bakers)
        Dish("dish-ak-1", "Strawberry Shake", "Rich, creamy strawberry milkshake blended with premium dairy and strawberry fruit chunks.", 99.0, isVeg = true, isBestSeller = true, restaurantId = "res-bakers"),
        Dish("dish-ak-2", "Choco Cheesecake", "Layers of luxurious chocolate base infused with real creamy cheesecake glaze.", 149.0, isVeg = true, isBestSeller = true, restaurantId = "res-bakers"),
        Dish("dish-ak-3", "Red Velvet Cake Slice", "Delightful red velvet sponge layered with silky smooth whipping cream and cheese sprinkles.", 120.0, isVeg = true, restaurantId = "res-bakers"),

        // Bhimas Indian Kitchen (res-bhimas)
        Dish("dish-bh-1", "Kaju Paneer Biryani", "Fragrant steamed saffron basmati rice layered with generous fried cashew nuts and tender paneer.", 199.0, isVeg = true, isBestSeller = true, restaurantId = "res-bhimas"),
        Dish("dish-bh-2", "Veg Dum Biryani Royal", "Fresh garden vegetables cooked with aromatic organic spices and layered in clay handi pot.", 149.0, isVeg = true, restaurantId = "res-bhimas"),
        Dish("dish-bh-3", "Dal Makhani Premium", "Slow 12-hour simmered delicious black lentils loaded with rich processing butter cream.", 120.0, isVeg = true, restaurantId = "res-bhimas"),

        // The Pizza Palace (res-pizza)
        Dish("dish-pz-1", "Paneer & Capsicum Pizza Mania", "Fresh green capsicum, tender juicy paneer cubes, and real mozzarella cheese on Classic Hand Tossed crust.", 99.0, isVeg = true, isBestSeller = true, restaurantId = "res-pizza"),
        Dish("dish-pz-2", "Tandoori Loaded Paneer Parcel", "Golden flaky pocket packed with spicy tandoori paneer stuffing and dynamic premium cheese sauce.", 75.0, isVeg = true, isBestSeller = true, restaurantId = "res-pizza"),
        Dish("dish-pz-3", "Tandoori Loaded Chicken Parcel", "Succulent spice-marinated bite-sized chicken tandoori stuffed inside flaky pocket pane.", 75.0, isVeg = false, isBestSeller = true, restaurantId = "res-pizza"),
        Dish("dish-pz-4", "Classic Fresh Pan", "Fresh hand-pan tossed basic mozzarella single-crust classic cheese pizza block.", 79.0, isVeg = true, restaurantId = "res-pizza"),
        Dish("dish-pz-5", "Tandoori Loaded Veg Taco (Single)", "Tandoori-spiced pocket taco stuffed with fresh colorful dynamic garden toppings.", 95.0, isVeg = true, isBestSeller = true, restaurantId = "res-pizza"),

        // Sri Venkateswara Sweets (res-sweets)
        Dish("dish-sw-1", "Plain Dosa", "Crispy, paper-thin golden crepe made from fermented rice-lentil flour served with fresh coconut chutney.", 49.0, isVeg = true, restaurantId = "res-sweets"),
        Dish("dish-sw-2", "Rava Dosa", "Delectable crispy semolina crepe mixed with fresh crushed green chilies, onions, and deep flavors.", 99.0, isVeg = true, restaurantId = "res-sweets"),
        Dish("dish-sw-3", "Motichoor Laddu (4pc)", "Mouth-watering orange sweet round laddus cooked perfectly with organic pure milk ghee.", 80.0, isVeg = true, isBestSeller = true, restaurantId = "res-sweets"),
        Dish("dish-sw-4", "Kesar Rasgulla (2pc)", "Spongy, extremely juicy cottage cheese dumplings floating in delicate saffron-perfumed simple sugar syrup.", 60.0, isVeg = true, restaurantId = "res-sweets")
    )
}
