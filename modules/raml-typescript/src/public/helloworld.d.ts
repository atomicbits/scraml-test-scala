/**
  * 
  * All rights reserved.
  * This is a custom license header test.
  * 
  */

export interface Adult extends Person {
  "job": string,
  "type": "Adult",
}
       

export interface Animal  {
  "gender": string,
  "_type": "Animal" | "Dog" | "Fish",
}
       

export interface Attributes  {
  "map": AttributesMap
}
       

export interface AttributesMap  {
  "lastName"?: string,
  "account"?: Array<string>,
  "callName"?: Array<string>,
  "firstName"?: string
}
       

export interface Author  {
  "lastName": string,
  "firstName": string
}
       

export interface BboxContainer  {
  "bbox": Array<Array<Array<number>>>
}
       

export interface Book  {
  "title": string,
  "author": Author,
  "isbn": string,
  "genre": string,
  "kind": "Book" | "ComicBook" | "ScienceFictionComicBook",
}
       

export interface Car  {
  "seats": number,
  "drive": Engine
}
       

export interface Cat  {
  "_type"?: CatType,
  "name"?: string
}
       

export type CatType = "Cat" 
       

export interface ComicBook extends Book, WithVillain {
  "hero": string,
  "kind": "ComicBook" | "ScienceFictionComicBook",
}
       

export interface ConnectionsInline  {
  
}
       

export interface ConnectionsInlineConnection  {
  "id": string
}
       

export interface ConnectionsInlineConnectionCollection  {
  "data": Array<ConnectionsInlineConnection>,
  "messages": Array<MessagesItems>
}
       

export interface ConnectionsInlineConnectionItem  {
  "data": ConnectionsInlineConnection,
  "messages": Array<MessagesItems>
}
       

export interface Crs  {
  "type": string,
  "properties": NamedCrsProperty
}
       

export interface Dog extends Animal {
  "canBark": boolean,
  "name"?: string,
  "_type": "Dog",
}
       

export interface EmptyObjectField  {
  "data": any,
  "message": string
}
       

export interface Engine  {
  "fuelType": Fuel,
  "power": number
}
       

export interface Error  {
  "message": string
}
       

export interface Fish extends Animal {
  "_type": "Fish",
}
       

export type Food = "rats" | "birds" | "beef" | "veggie" | "pork" 
       

export interface Fuel  {
  "cost": number
}
       

export interface Geometry  {
  "crs"?: Crs,
  "bbox"?: Array<number>,
  "type": "Geometry" | "GeometryCollection" | "Polygon" | "MultiPolygon" | "MultiPoint" | "Point" | "MultiLineString" | "LineString",
}
       

export interface GeometryCollection extends Geometry {
  "geometries": Array<Geometry>,
  "type": "GeometryCollection",
}
       

export type Heroes = "Spyderman" | "Superman" | "Daredevil" 
       

export interface LineString extends Geometry {
  "coordinates": Array<Array<number>>,
  "type": "LineString",
}
       

export interface Link  {
  "method": Method,
  "href": string,
  "accept"?: string
}
       

export interface LongPagedList<T,U>  {
  "count": number,
  "owner": U,
  "elements": Array<T>
}
       

export interface Mamal  {
  "birthday": string,
  "type": "Mamal" | "Person" | "Adult",
}
       

export interface ManyFields  {
  "e": string,
  "s": string,
  "x": string,
  "n": string,
  "j": string,
  "y": string,
  "t": string,
  "u": string,
  "f": string,
  "a": string,
  "m": string,
  "i": string,
  "v": string,
  "q": string,
  "b": string,
  "g": string,
  "l": string,
  "p": string,
  "c": string,
  "h": string,
  "r": string,
  "w": string,
  "k": string,
  "o": string,
  "z": string,
  "d": string
}
       

export interface MessagesItems  {
  "id": string
}
       

export type Method = "GET" | "PUT" | "POST" | "DELETE" | "HEAD" | "CONNECT" | "TRACE" | "OPTIONS" | "new" | "8Trees" | "hy-phen" | "spa ce" 
       

export interface MultiLineString extends Geometry {
  "coordinates": Array<Array<Array<number>>>,
  "type": "MultiLineString",
}
       

export interface MultiPoint extends Geometry {
  "coordinates": Array<Array<number>>,
  "type": "MultiPoint",
}
       

export interface MultiPolygon extends Geometry {
  "coordinates": Array<Array<Array<Array<number>>>>,
  "type": "MultiPolygon",
}
       

export interface MyObject  {
  "data"?: Array<MyOtherObject>
}
       

export interface MyOtherObject  {
  "name": string,
  "typeName": string,
  "placeList": string
}
       

export interface NamedCrsProperty  {
  "name": string
}
       

export interface PagedList<T,U>  {
  "count": number,
  "owner"?: U,
  "elements": Array<T>
}
       

export interface Person extends Mamal {
  "lunchtime": string,
  "hobbies": Array<string>,
  "type": "Person" | "Adult",
}
       

export interface Point extends Geometry {
  "coordinates": Array<number>,
  "type": "Point",
}
       

export interface Polygon extends Geometry {
  "coordinates": Array<Array<Array<number>>>,
  "type": "Polygon",
}
       

export interface SciFiComicBook extends ComicBook {
  "era": string,
  "kind": "ScienceFictionComicBook",
}
       

export interface SimpleForm  {
  "age"?: number,
  "lastname": string,
  "firstname": string
}
       

export interface Stars  {
  "fans": LongPagedList<User,number>,
  "name": string
}
       

export interface StrangeChars  {
  "with/slash": string,
  "with~tilde"?: string,
  "with space": string
}
       

export interface Tree  {
  "children": Array<Tree>,
  "value": string
}
       

export interface TwentyThreeFields  {
  "twenty": string,
  "eighteen": string,
  "four": string,
  "three": string,
  "twelve": string,
  "eleven": string,
  "two": string,
  "twentythree": string,
  "fifteen": string,
  "seventeen": string,
  "six": string,
  "seven": string,
  "twentytwo": string,
  "sixteen": string,
  "ten": string,
  "fourteen": string,
  "five": string,
  "thirteen": string,
  "twentyone": string,
  "nineteen": string,
  "nine": string,
  "one": string,
  "eight": string
}
       

export interface User  {
  "birthday": string,
  "homePage"?: Link,
  "age": number,
  "lastName": string,
  "firstName": string,
  "id": string,
  "address"?: UserDefinitionsAddress,
  "fancy-field"?: string,
  "other"?: any
}
       

export interface UserDefinitionsAddress  {
  "city": string,
  "state": string,
  "streetAddress": string
}
       

export interface WithVillain  {
  "villain": string,
  "kind": "WithVillain" | "ComicBook" | "ScienceFictionComicBook",
}
       

export interface Zoo  {
  "name": string,
  "If-Modified-Since"?: string,
  "animals"?: Array<Animal>,
  "lunchtime": string,
  "visitors"?: PagedList<User,number>,
  "fireworks": string,
  "created": string
}
       