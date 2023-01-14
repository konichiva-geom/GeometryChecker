# Маркетинг

Чем это лучше ГДЗ?   Кажется если человек не может понять, правильно ли у него по ГДЗ, то и прогу написать не сможет.
Можно по-разному решить задачу, а ГДЗ дает одно решение

# Интерпретация

Н ужно идти параллельно с деревом, и в траай кетч ловить исключение, находясь в дереве. Выводить range элементов этого
поддерева.

# Фронт

1. Сделать, что в plain можно писать markdown. Plain теперь multiline comment над программой. Юзать
   можно [эту](https://openbase.com/js/marked) либу.

2. Добавлять значок градуса справа сверху чисел, когда сравниваются углы: `ABC > 90˚°`

# Парсить плаин текст сразу в descr
# Углы

как обозначать тупой угол? - ~~[90, 1, ...] - вот так~~ A > 90
как обозначать, что A > B?:
A = [0, 1, 1, ...]
B = [0, 1, 0, ...]

Если пишем условие и A и B пока пустые, то можно так. Иначе через сравнение

# Сравнения

Сравнение это мапа, которая по вектору левой части выдает правую часть и все слагаемые.

Для создания новых сравнений на основе существующих:

1. Замена левой части:
   a + b - 2c > 3e - 2b + z => 2z < 2a + 6b - 4c - 6e

   Идейно мы создаем вектор разности двух частей, оставляем слева нужный вектор,
   если что домножаем/делим, остальное переносим вправо (справа у нас тоже может быть z, так что
   нужно
   аккуратно домножать и делить). Не забываем про знак.

2. Замена выражения:
   a + b - 2c > 3e - 2b + z, z = a => a + b - 2c > 3e - 2b + a

   **В этом случае, меняем в левой части и в правой части один вектор на другой. Я не уверен, что
   так можно (как и в первом случае)** (замена может быть неравенством, нужно учитывать)

### define statement

Users might not like names of some theorems. If so, they can rename them with #define old_name
new_name.

Each user can have a file of their redefinitions, and when browsing solutions of others, he will
see their code with his redefinitions applied.

Example:

```
// suppose we have theorem is_collinear:
#define col is_collinear
col(AB, CD) // will call is_collinear(AB, CD)
```

### Drawing

Is needed to check how points are positioned. This is needed for:

1. Check angles relation (DAB == DAC - BAC)
2. Check segments relation (AB == AC - BC)

### Creating variables :=

Variable can be of type: Point, Line, Angle, Segment

Point can be created:

1. As intersection of lines (or anything actually) `l1` and `l2`: `P := Point(l1, l2)`
2. Between points: `P := Point(segment AB)`
3. On line: `P := Point(AB)`
4. On ray: `P := Point(ray AB)`
5. In triangle `P := Point(triangle ABC)`