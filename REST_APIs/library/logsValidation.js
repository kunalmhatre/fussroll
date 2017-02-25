/******
Below are the functions for updateLog API related fields validation.
******/
exports.checkLogs = function(category, log, meta) {
	if(category === "feeling") {
		return feeling(log, meta);
	}
	else if(category === "place") {
		return place(log, meta);
	}
	else if(category === "activity") {
		return activity(log);
	}
	else if(category === "game") {
		return game(log, meta);
	}
	else if(category === "food") {
		return food(log, meta);
	}
	else if(category === "drink") {
		return drink(log, meta);
	}
	else
		return false;
};

function feeling(log, meta) {

	if(meta === "Feeling" || meta === "Felt") {

		var logs = [
					"good",
			        "great",
			        "crazy",
			        "sad",
			        "scared",
			        "emotional",
			        "funny",
			        "alone",
			        "irritated",
			        "amazed",
			        "bored",
			        "angry",
			        "cold",
			        "playful",
			        "very bad",
			        "nervous",
			        "enjoyable",
			        "sleepy",
			        "jealous",
			        "mischievous",
			        "hungry",
			        "too angry",
			        "tensed",
			        "ill",
			        "better",
			        "huggable",
			        "romantic",
			        "flirty",
			        "sorry",
			        "strange",
			        "idiot",
			        "upset",
			        "dizzy",
			        "embarrassed",
			        "rich",
			        "hot",
			        "naughty",
			        "excited",
			        "tempted",
			        "frowned",
			        "blessed",
			        "lonely",
			        "neutral",
			        "weird",
			        "annoyed",
			        "ashamed",
			        "gross",
			        "swaggy",
			        "okay",
			        "surprised",
			        "sick",
			        "awesome",
			        "astonished",
			        "studious",
			        "silly",
			        "tired",
			        "thoughtful",
			        "anguished",
			        "confused",
			        "sarcastic",
			        "lovely",
			        "worst",
			        "worried"
					];

		if(logs.indexOf(log) != -1)
			return true;
		else
			return false;

	}	
	else
		return false;

};

function place(log, meta) {

var logs = [
			"college",
			"office",
			"gym",
			"movie theater",
			"school",
			"clinic",
			"restaurant",
			"hotel",
			"park",
			"shop",
			"hospital",
			"bank",
			"funeral"
			];

if(log === "home") {
	if(meta === "Coming back" || meta === "At" || meta === "Away from")
		return true;
	else
		return false;
}
else if(logs.indexOf(log) != -1 ) {
	if(meta === "Going to the" || meta === "In the" || meta === "Left the")
		return true;
	else
		return false;
}
else if(log === "friend's house" || log === "relative's house") {
	if(meta === "Going to my" || meta === "At my" || meta === "Left my")
		return true;
	else
		return false;
}
else if(log === "beach" && (meta === "Going to the" || meta === "At the" || meta === "Left the")) {
	return true;
}
else
	return false;

};

function activity(log) {

var logs = [
			"Listening to songs",
			"Relaxing",
			"Watching television",
			"Cooking",
			"Going to sleep",
			"Reading a book",
			"Dating",
			"Partying",
			"Celebrating birthday",
			"Working at the office",
			"Watching a movie",
			"Writing",
			"Studying",
			"Exercising",
			"Jogging",
			"Going out for a walk",
			"Camping",
			"Painting",
			"Getting married"
			];

if(logs.indexOf(log) != -1)
	return true;
else
	return false;

};

function game(log, meta) {

var logs = [
			"cricket",
			"football",
			"badminton",
			"table tennis",
			"video game",
			"chess",
			"cards",
			"bowling",
			"pool",
			"basketball",
			"golf",
			"baseball",
			"rugby",
			"hockey",
			"bow and arrow",
			"frisbee",
			"tennis"
			];

if(logs.indexOf(log) != -1) {
	if(meta === "Playing" || meta === "Played" || meta === "Going to play")
		return true;
	else
		return false;
}
else
	return false;

};

function food(log, meta) {

var logs = [
	        "pizza",
	        "burger",
	        "salad",
	        "bread",
	        "pancakes",
	        "french fries",
	        "ice cream",
	        "hot dog",
	        "taco",
	        "burrito",
	        "whole bread",
	        "chicken",
	        "rice",
	        "spaghetti",
	        "noodles",
	        "custard pudding",
	        "popcorn",
	        "doughnut",
	        "cake",
	        "sushi",
	        "shrimp",
	        "meat",
	        "bacon",
	        "cheese",
	        "birthday cake",
	        "chocolate",
	        "candy",
	        "cookies"
			];

if(log === "breakfast" || log === "lunch" || log === "dinner") {
	if(meta === "Having" || meta === "Had")
		return true;
	else
		return false;
}
else if(log === "omelette") {
	if(meta === "Eating an" || meta === "Ate an")
		return true;
	else
		return false;
}
else if(log === "sandwich") {
	if(meta === "Eating a" || meta === "Ate")
		return true;
	else
		return false;
}
else if(logs.indexOf(log) != -1) {
	if(meta === "Eating" || meta === "Ate")
		return true;
	else
		return false;
}
else 
	return false;

};

function drink(log, meta) {

var logs = [
			"juice",
			"beer",
			"martini",
			"wine",
			"milk",
			"liquor",
			"champagne"
			];

if(logs.indexOf(log) != -1) {
	if(meta === "Drinking" || meta === "Drank")
		return true;
	else
		return false;
}
else if(log === "tea" || log === "coffee") {
	if(meta === "Having" || meta === "Had")
		return true;
	else
		return false;
}
else
	return false;

};

