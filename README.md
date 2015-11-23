### Assignment №2 - Introduction to Service Design and Engineering

#### What is it? ####

This assignment is about RESTful services based on Labs №5-7. There is implemented many of CRUD operations by using HTTP protocol. 

###List of CRUD operations:

#####Request #1: GET /person will list all the people in database
#####Request #2: GET /person/{id} will give all the personal information plus current measures of person identified by {id}
#####Request #3: PUT /person/{id} will update the personal information of the person identified by {id}
#####Request #4: POST /person will create a new person and return the newly created person with its assigned id
#####Request #5: DELETE /person/{id} will delete the person identified by {id}
#####Request #6: GET /person/{id}/{measureType} will return the list of values of {measureType} for person identified by {id}
#####Request #7: GET /person/{id}/{measureType}/{mid} willreturn the value of {measureType} identified by {mid} for person identified by {id}
#####Request #8: POST /person/{id}/{measureType} will save a new value for the {measureType} of person identified by {id} and archive the old value in the history
#####Request #9: GET /measureTypes will return the list of measures your model supports in the XML and JSON formats

#### How to run server ####
The server is running in Heroku Cloud Platform now. 
###**[https://immense-caverns-1125.herokuapp.com/sdelab](https://immense-caverns-1125.herokuapp.com/sdelab)**
www.heroku.com
In fact, you can run it on your local machine my using this command:

	ant clean install
	ant start


### How to run the client?
Open the command line and run the following command in the root directory of the program:

	ant execute.client


###### P.S. I worked alone
