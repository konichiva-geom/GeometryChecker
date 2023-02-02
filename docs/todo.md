### Development
1. interpreter is becoming a pipeline too. Maybe convert it to pipeline and move part of its logic to a separate class

### Dev questions
1. SegmentPointCollection, ArcPointCollection addPoints(). is it bad if added point is in [bounds]?
2. Similar for RayPointCollection:  is it bad if added point is [start]?
3. How to work with sin, cos, and should we really cover it?

### Not development
1. check wolfram alpha paid how can he check geom, geogebra

### After development
1. delete Pipeline.code field in production

### Frontend
1. if theorem not found, make search by distance and suggest other variants:
   Theorem merge_projedvions not found, maybe you meant merge_projections(...)?
2. Сделать, что в plain можно писать markdown. Plain теперь multiline comment над программой. Юзать
   можно [эту](https://openbase.com/js/marked) либу.
3. обавлять значок градуса справа сверху чисел, когда сравниваются углы: `ABC > 90˚°`
4. Users might not like names of some theorems. If so, they can rename them with #define old_name
   new_name. <br>Each user can have a file of their redefinitions, and when browsing solutions of others, he will
see their code with his redefinitions applied. 
Example:

```
// suppose we have theorem is_collinear:
#define col is_collinear
col(AB, CD) // will call is_collinear(AB, CD)
```
