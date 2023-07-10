(()=>{
	let refresh = ()=>{
		fetch("/")
			.then(x => x.text())
		.then(y => {
			let div=document.createElement('div');
			div.innerHTML = y;
			document.querySelector(".messages").innerHTML = div.querySelector(".messages").innerHTML;
		});
	}
	
	setInterval(refresh,1000);

})();
