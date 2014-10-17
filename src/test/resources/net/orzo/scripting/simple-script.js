var person,
	objectList,
	itemList,
	pi;
	
person = {
	
	firstName : 'John',
	
	lastName : 'Doe',
	
	age : 37,
	
	weight : 83.9,
	
	human: true,
	
	sayHello : function() {
		return person.firstName + ' says hello!';
	}
};

function Animal(type) {
	this.type = type;
}

objectList = [new Animal('dog'), new Animal('cat'), new Animal('squirrel')];

itemList = [ person, /* why  1 converts to float?? */ 2, "second", [3, 'item four'] ];

pi = 3.1416;