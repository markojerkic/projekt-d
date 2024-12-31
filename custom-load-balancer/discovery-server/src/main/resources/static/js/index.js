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

function watchHtmxTest() {
	htmx.on("htmx:afterSwap", function (event) {
		/** @type {XMLHttpRequest} */
		const responseXHR = event.detail.xhr;
		const instanceId = getHeader("X-Lb-instance", responseXHR);

		const sourceELementAttributes = event.detail.elt?.attributes;

		const elementName =
			sourceELementAttributes && sourceELementAttributes["data-name"]?.value;

		if (elementName) {
			console.log("HTMX has swapped elements", elementName);
			console.log("From instance", instanceId);
		} else {
			console.warn("HTMX has swapped elements, but not from test fetcher");
		}
	});
}

function getHeader(name, responseXHR) {
	const headers = responseXHR.getAllResponseHeaders();
	const header = headers
		.split("\n")
		.filter((header) => header.includes(name.toLowerCase()))
		.join("");
	return header;
}

window.addEventListener("load", function () {
	updateTimes();
	watchHtmxTest();
});
