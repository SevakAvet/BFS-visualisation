# BFS-visualisation

Simple BFS visualisation on JavaFX.

Sample input file:

```
size 450 450

start 1

1 2
2 1 3 5
3 2
4 6
5 2 6 7
6 4 5 7 8
7 5 6 12 13
8 6 9 11
9 8 10
10 9 11
11 8 10
12 7
13 7 14
14 13

pos 1 25 300
pos 2 50 350
pos 3 100 300
pos 4 100 200
pos 5 150 340
pos 6 160 300
pos 7 250 355
pos 8 200 150
pos 9 150 75
pos 10 300 50
pos 11 350 150
pos 12 300 250
pos 13 350 300
pos 14 370 370
```

* **size w h** - size of screen
* **start x** - define starting vertex
* **a [b, c... z]** - list of a's neighbours
* **pos v x y** - 2D-position of vertex **v**

![Imgur](http://i.imgur.com/JdLTOhk.gifv)
