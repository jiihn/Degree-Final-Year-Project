firebase.auth().onAuthStateChanged(function(user) {
	  if (user) {
		// User is signed in.
		document.getElementById("login_div").style.display = "none";
		document.getElementById("logged_div").style.display = "block";
		var user = firebase.auth().currentUser;

		if(user != null){
			var email_id = user.email;
		}

	  } else {
		// No user is signed in.

		document.getElementById("login_div").style.display = "block";
		document.getElementById("logged_div").style.display = "none";

	  }
	});
	
function login(){

	var userEmail = document.getElementById("email").value;
	var userPass = document.getElementById("pwd").value;
	
	if(userEmail == "freelancermobileapplication@gmail.com"){

		firebase.auth().signInWithEmailAndPassword(userEmail, userPass).catch(function(error) {
			// Handle Errors here.
		var errorCode = error.code;
		var errorMessage = error.message;

		window.alert("Error : " + errorMessage);

			// ...
		});
	} else {
		window.alert("You are not allowed to sign in");
	}
}

function logout(){
  firebase.auth().signOut();
}

function getData(){
	var ref = firebase.database().ref("Category");
	var myTable = "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>Category ID</th><th>Category Name</th><th>Action</th></tr></thead><tbody>";
	
	ref.on("value", function(snapshot){
		snapshot.forEach(function(childSnapshot){
			var item = childSnapshot.val();
			var catName = childSnapshot.val().name;
			var catId = childSnapshot.val().id;
			myTable += "<tr>";
			myTable += "<td>" + catId + "</td>";
			myTable += "<td>" + catName + "</td>";
			myTable += "<td><button onclick=\"deleteData('" + catId + "')\" class=\"btn btn-primary\">Delete</button></td>";
			myTable += "</tr>";
		});
	});
	
	myTable += "</tbody></table></div>";

	document.getElementById("writeData_div").innerHTML = myTable;
	
}

function getOrder(){
	var ref = firebase.database().ref("Order");
	var myTable = "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>Order ID</th><th>Buyer</th><th>Action</th></tr></thead><tbody>";
	
	ref.on("value", function(snapshot){
		snapshot.forEach(function(childSnapshot){
			var item = childSnapshot.val();
			var order = childSnapshot.val().orderId;
			var buyer = childSnapshot.val().buyer;
			
			myTable += "<tr>";
			myTable += "<td>" + order + "</td>";
			myTable += "<td>" + buyer + "</td>";
			myTable += "<td><button onclick=\"getOrderDetails(\'"+ order +"\')\" class=\"btn btn-primary\">View</button></td>";
			myTable += "</tr>";
		});
	});
	
	myTable += "</tbody></table></div>";

	document.getElementById("writeOrder_div").innerHTML = myTable;
	
}

function getUser(){
	var ref = firebase.database().ref("Users");
	var userTable = "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>User ID</th><th>First Name</th><th>Last Name</th><th>Status</th><th>Action</th></tr></thead><tbody>";
			
	ref.on("value", function(snapshot){
		snapshot.forEach(function(childSnapshot){
			var item = childSnapshot.val();
			var verified = childSnapshot.val().verified;
			var userId = childSnapshot.val().id;
			var userFName = childSnapshot.val().first_name;
			var userLName = childSnapshot.val().last_name;
			var userPNum = childSnapshot.val().phone_number;
			var userCity = childSnapshot.val().city;
			var userState = childSnapshot.val().state;
			if(verified == false){
				userTable += "<tr>";
				userTable += "<td>" + userId + "</td>";
				userTable += "<td>" + userFName + "</td>";
				userTable += "<td>" + userLName + "</td>";
				userTable += "<td>Not Verified</td>";
				userTable += "<td><button onclick=\"getDetails(\'"+ userId +"\')\" class=\"btn btn-primary\">View</button></td>";
				userTable += "</tr>";
			}
		});
	});
			
	userTable += "</tbody></table></div>";

	document.getElementById("userData_div").innerHTML = userTable;
	
	var verifiedTable = "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>User ID</th><th>First Name</th><th>Last Name</th><th>Status</th><th>Action</th></tr></thead><tbody>";
	
	ref.on("value", function(snapshot){
		snapshot.forEach(function(childSnapshot){
			var item = childSnapshot.val();
			var verified = childSnapshot.val().verified;
			var userId = childSnapshot.val().id;
			var userFName = childSnapshot.val().first_name;
			var userLName = childSnapshot.val().last_name;
			var userPNum = childSnapshot.val().phone_number;
			var userCity = childSnapshot.val().city;
			var userState = childSnapshot.val().state;
			if(verified == true){
				verifiedTable += "<tr>";
				verifiedTable += "<td>" + userId + "</td>";
				verifiedTable += "<td>" + userFName + "</td>";
				verifiedTable += "<td>" + userLName + "</td>";
				verifiedTable += "<td>Verified</td>";
				verifiedTable += "<td><button onclick=\"getDetails(\'"+ userId +"\')\" class=\"btn btn-primary\">View</button></td>";
				verifiedTable += "</tr>";
			}
		});
	});
				
	verifiedTable += "</tbody></table></div>";
	document.getElementById("verified_div").innerHTML = verifiedTable;
}
		
function getDetails(userId){
	var ref = firebase.database().ref("Users/" + userId );
	var displayDetails = "";
	
	ref.once("value")
		.then(function(snapshot){
			var item = snapshot.val();
			var verified = snapshot.val().verified;
			var userId = snapshot.val().id;
			var imageURL = snapshot.val().imageURL;
			var userFName = snapshot.val().first_name;
			var userLName = snapshot.val().last_name;
			var userPNum = snapshot.val().phone_number;
			var userAddress = snapshot.val().address;
			var userCity = snapshot.val().city;
			var userState = snapshot.val().state;
			var icNum = snapshot.val().icNum;
			var icURL = snapshot.val().icURL;
			
			displayDetails += "<h2>"+ userFName +"</h2>"
			displayDetails += "<h2>"+ userLName +"</h2>"
			if(imageURL == "default"){
				displayDetails += "<h4>No Profile Image</h4>"
			} else{
				displayDetails += "<img src=\""+ imageURL +"\" width=\"150px\" height = \"150px\">"
			}
			displayDetails += "<br><p><b>Phone Number: </b>"+ userPNum +"</p>"
			displayDetails += "<p><b>Address: </b>"+ userAddress +"</p>"
			displayDetails += "<p><b>City: </b>"+ userCity +"</p>"
			displayDetails += "<p><b>State: </b>"+ userState +"</p>"
				
			displayDetails += "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>IC Image</th><th>IC Number</th></tr></thead><tbody>"
			if(icNum == "none" && icURL=="default"){
				displayDetails += "<tr>";
				displayDetails += "<td>No Image Uploaded</td>";
				displayDetails += "<td>No IC Number Submitted</td>";
				displayDetails += "</tr>";
			} else{
				displayDetails += "<tr>";
				displayDetails += "<td><img src=\""+ icURL +"\" width=\"150px\" height = \"150px\"></td>";
				displayDetails += "<td>"+ icNum +"</td>";
				displayDetails += "</tr>";
			}
					
			displayDetails += "<button onclick=\"verifyUser(\'"+ userId +"\')\" class=\"btn btn-primary\">Verify</button>&ensp;"
			displayDetails += "<button onclick=\"rejectUser(\'"+ userId +"\')\" class=\"btn btn-primary\">Reject</button>"
			
			document.getElementById("details_div").innerHTML = displayDetails;
		});
}

function getOrderDetails(order){
	var ref = firebase.database().ref("Order/" + order );
	var displayDetails = "";
	
	ref.once("value")
		.then(function(snapshot){
			var item = snapshot.val();
			var orderId = snapshot.val().orderId;
			var complete = snapshot.val().complete;
			var paid = snapshot.val().paid;
			var rated = snapshot.val().rated;
			var buyer = snapshot.val().buyer;
			var seller = snapshot.val().seller;
			var verified = snapshot.val().verified;
			
			displayDetails += "<h2> Order ID: "+ orderId +"</h2>"
			displayDetails += "<br><p><b>Buyer: </b>"+ buyer +"</p>"
			displayDetails += "<p><b>Seller: </b>"+ seller +"</p>"
				
			displayDetails += "<div class=\"container\"><table class=\"table table-bordered\"><thead><tr><th>Paid</th><th>Complete</th><th>Rated</th><th>Verified</th></tr></thead><tbody>"
			displayDetails += "<tr>";
			displayDetails += "<td>"+ paid +"</td>";
			displayDetails += "<td>"+ complete +"</td>";
			displayDetails += "<td>"+ rated +"</td>";
			displayDetails += "<td>"+ verified +"</td>";
			displayDetails += "</tr>";
			
			document.getElementById("orderDetails_div").innerHTML = displayDetails;
		});
}
		
function verifyUser(userId){
	const fb = firebase.database().ref("Users/" + userId );
		
	fb.update({ verified: true });
	fb.update({ rejected: false });
	
	window.alert("User Successfully Verified");
}

function rejectUser(userId){
	const fb = firebase.database().ref("Users/" + userId );
		
	fb.update({ rejected: true });
	fb.update({ verified: false });
	
	window.alert("User Application Rejected");
}

function deleteData(catId){	
	firebase.database().ref("Category").child(catId).remove();
	location.reload();
}

function addCategory(){
	var database = firebase.database();
	var catRef = database.ref("Category").push()
	var getKey = catRef.key;
			
	catRef.set({
		name: document.getElementById("categoryName").value,
		id: getKey
	});
	
	window.alert("Successfully add category");
	
	document.getElementById("categoryName").value.innerHTML = "";
	
	document.getElementById("addCat_div").style.display='none';
	document.getElementById("viewCat_div").style.display='block';
}

function ShowContent(content){
	document.getElementById("home_div").style.display='none';
	document.getElementById("addCat_div").style.display='none';
	document.getElementById("viewCat_div").style.display='none';
	document.getElementById("user_div").style.display='none';
	document.getElementById("order_div").style.display='none';
	document.getElementById(content).style.display='block';
}