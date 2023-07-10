(()=>{
	let inp = document.querySelector(".header textarea");

inp.addEventListener("keypress", (evt) => {
	if (evt.key === "Enter" && evt.shiftKey == true) {
		
		evt.preventDefault();

		let formData = new FormData();
		formData.append('msg', inp.value);

		fetch("/rest/messages",
			{
				body: formData,
				method: "post"
			});

		inp.value = "";
		
	}
});
	
})();
