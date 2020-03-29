# Currency Converter

## Structure

3 packages and Boot file: model, service, model;
Model: consists of the required object models and their json encoders and decoders;
Service: the main logic of the project. Consists of the functions which perform conversion;
Route: created the main endpoints

Boot object calls main route and extends scala's App trait 

All build configurations are written in the build.sbt file.
  
## Endpoints

1) Sell:  exchange/sell?currency=[CurrencyToSell]&rate=[AmoundToDell]&date=[Date]
Examples:
 - exchange/sell?currency=USD&rate=30.0&date=01.07.2019
 - exchange/sell?currency=USD&rate=30.0
"date" parameter is optional: in case it is not specified the conversion for current date
will be performed 
 "currency" and "rate" parameters are required;
 
2)Buy: exchange/buy?currency=[CurrencyToSell]&rate=[AmoundToDell]&date=[Date] - requirements are 
the same as for Sell;

## Errors
- PBError("Service temporary unavailable") - service cannot connect to PryvatBank API;
- CannotParseJson("Cannot parse json") - not possible to parse data from PryvatBank API;
- NoDataForDay("No conversion results for this day") - information from PryvatBank API 
not available for specified date;
- UnavailableCurrency("Unfortunately, unavailable conversion for this currency") - currency specified in endpoint either
wrong or not available for that day;

## Aspects and Improvements :
 - API uses Futures for concurrency, however it could be more efficient probably to 
 add Actors (Akka Actors) level between route and service in order to prevent crash under 
 high load;
 - API has errors classes to prevent from specific errors which could happen. However, it doesn't 
 have specific login to overdue the next errors:
 a) user can input rate with negative value;
 b) PryvatBank API could be not available: in this case the PBError is shown however maybe it is worth to get data from
 other API in case Pryvat is not available;
 b) should be validation added (probably on client side) for 'date' parameter as it should look like: "dd.MM.yyy" (01.12.2019);
 
## How to check:
Postman: no auth needed
  
 

