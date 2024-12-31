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
			const selectedColor = getColorForInstanceId(instanceId);
			addInstanceIdAsAttribute(instanceId, event.detail.elt);
			addSelectedColorToCss(instanceId, selectedColor);
			setBackgroundColorForInstance(
				instanceId,
				event.detail.elt,
				selectedColor,
			);
		} else {
			console.warn("HTMX has swapped elements, but not from test fetcher");
		}
	});
}

/**
 * @param {string} instanceId
 * @param {HTMLElement} element
 * @returns {void}
 */
function addInstanceIdAsAttribute(instanceId, element) {
	if (!element.parentElement) {
		console.error("Element does not have a parent element", element);
		return;
	}
	element.parentElement.setAttribute("data-instance-id", instanceId);
}

function addSelectedColorToCss(instanceId, selectedColor) {
	let instanceStyle = document.getElementById("instance-style");
	if (!instanceStyle) {
		instanceStyle = document.createElement("style");
		instanceStyle.id = "instance-style";
		document.head.appendChild(instanceStyle);
	}

	instanceStyle.innerHTML += `
        [data-instance-id="${instanceId}"] {
            background-color: ${selectedColor};
        }
    `;
}

/**
 * @param {string} instanceId
 * @param {HTMLElement} element
 * @param {string} selectedColor
 * @returns {void}
 */
function setBackgroundColorForInstance(instanceId, element, selectedColor) {
	if (!element.parentElement) {
		console.error("Element does not have a parent element", element);
		return;
	}

	element.parentElement.style.backgroundColor = selectedColor;
	console.log(
		"Setting background color",
		selectedColor,
		"for instance",
		instanceId,
	);
}

function getHeader(name, responseXHR) {
	const headers = responseXHR.getAllResponseHeaders();
	const header = headers
		.split("\n")
		.filter((header) => header.includes(name.toLowerCase()))
		.join("");
	return header;
}

const availableColors = [
	"#B0C4DE",
	"#8B4513",
	"#2F4F4F",
	"#556B2F",
	"#708090",
	"#D2691E",
	"#6A5ACD",
	"#8B0000",
	"#800000",
	"#483D8B",
	"#2E8B57",
	"#A52A2A",
	"#8A2BE2",
	"#7FFF00",
	"#A9A9A9",
	"#A52A2A",
	"#000080",
	"#808000",
	"#006400",
	"#8B008B",
];

const assignedColors = new Map();

function getColorForInstanceId(instanceId) {
	if (assignedColors.has(instanceId)) {
		return assignedColors.get(instanceId); // Return already assigned color
	}

	// Find the first color that hasn't been assigned
	for (let color of availableColors) {
		if (![...assignedColors.values()].includes(color)) {
			assignedColors.set(instanceId, color); // Assign color to the instanceId
			return color;
		}
	}

	// If all are used, clear the map and try again
	assignedColors.clear();

	document.getElementById("instance-style")?.remove();

	return getColorForInstanceId(instanceId);
}

window.addEventListener("load", function () {
	updateTimes();
	watchHtmxTest();
});
