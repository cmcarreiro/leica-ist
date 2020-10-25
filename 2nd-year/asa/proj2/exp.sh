#!/bin/bash
python2 p2_gerador.py -N 10 -M 10 -C 3 -S 3 > gerador1.txt
./proj2_exp.out < gerador1.txt
python2 p2_gerador.py -N 20 -M 20 -C 6 -S 6 > gerador2.txt
./proj2_exp.out < gerador2.txt
python2 p2_gerador.py -N 30 -M 30 -C 10 -S 10 > gerador3.txt
./proj2_exp.out < gerador3.txt
python2 p2_gerador.py -N 40 -M 40 -C 13 -S 13 > gerador4.txt
./proj2_exp.out < gerador4.txt
python2 p2_gerador.py -N 50 -M 50 -C 16 -S 16 > gerador5.txt
./proj2_exp.out < gerador5.txt
python2 p2_gerador.py -N 60 -M 60 -C 20 -S 20 > gerador6.txt
./proj2_exp.out < gerador6.txt
python2 p2_gerador.py -N 70 -M 70 -C 23 -S 23 > gerador7.txt
./proj2_exp.out < gerador7.txt
python2 p2_gerador.py -N 80 -M 80 -C 26 -S 26 > gerador8.txt
./proj2_exp.out < gerador8.txt
python2 p2_gerador.py -N 90 -M 90 -C 30 -S 30 > gerador9.txt
./proj2_exp.out < gerador9.txt
python2 p2_gerador.py -N 100 -M 100 -C 33 -S 33 > gerador10.txt
./proj2_exp.out < gerador10.txt
