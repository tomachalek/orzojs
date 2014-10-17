importPackage(net.orzo.scripting);

var person = {
	
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

var objectList = [new Animal('dog'), new Animal('cat'), new Animal('squirrel')];

var itemList = [ person, /* why  1 converts to float?? */ 2, "second", [3, 'item four'] ];

var pi = 3.1416;

var javaObjectList = [ new Vehicle(), new Bus() ];