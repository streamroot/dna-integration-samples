window.addEventListener('load', function()
{
	var header = document.getElementsByTagName("header")[0];

	header.innerHTML= "<ul><li><img src='${window.location.origin}/../../assets/img/logo_typo_long.svg' height=30></li>"+
						"<li>" + document.title +"</li>"+
						"<li><a href='#'' target='_blank'>New user</a></li>"+
						"<li><a href='${window.location.origin}/../../index.html'>Back</a></li>";
});