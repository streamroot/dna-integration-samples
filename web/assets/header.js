window.addEventListener('load', function () {
	var player = location.pathname.split('/').slice(-1)[0];
	var header = document.getElementsByTagName("header")[0];

	if (player == 'mpx.html' || player == 'kaltura.html') {
		header.innerHTML = "<ul><li><img src='${window.location.origin}/../../assets/img/logo_typo_long.svg' height=30></li>" +
			"<li>" + document.title + "</li>" +
			"<li><a href='#'' target='_blank'>SIMULATE A USER</a></li>" +
			"<li><a href='../index.html'>BACK</a></li>";
	} else {
		header.innerHTML = "<ul><li><img src='${window.location.origin}/../../assets/img/logo_typo_long.svg' height=30></li>" +
			"<li>" + document.title + "</li>" +
			"<li><a href='../index.html'>BACK</a></li>";
	}

});