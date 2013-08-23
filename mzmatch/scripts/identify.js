// http://localhost:5984/compounds/_design/identify/_view/all?key="{_id}"
// http://localhost:5984/compounds/_design/identify/_view/mass?key="100.016044"
// http://localhost:5984/compounds/_design/identify/_view/mass?startkey="100.016"&endkey="100.03"

{
   "_id": "_design/identify",
   "language": "javascript",
   "views": {
       "mass": {
           "map": "function(doc) { if (doc.monoisotopic_weight !== null) { emit(doc.monoisotopic_weight, null); } }"
       }
   }
}