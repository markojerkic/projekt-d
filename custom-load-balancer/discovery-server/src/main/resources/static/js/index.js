// watch for changes in the dom
// for each <time /> element, read the datetime attribute
// and update the innerHTML with the formatted date

function updateTimes() {
	var times = document.querySelectorAll("time");
	for (var i = 0; i < times.length; i++) {
		var time = times[i];
		time.innerHTML = new Date(time.getAttribute("datetime")).toLocaleString();
	}
}

window.addEventListener("load", function () {
	alert("Hello, World!");
	updateTimes();
});
