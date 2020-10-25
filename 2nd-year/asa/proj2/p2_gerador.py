#!/usr/bin/python2
import argparse
import random

parser = argparse.ArgumentParser(description='Graph instance generator.')

parser.add_argument('-N', type=int, default=1,
                    help='Maximum number of streets')
parser.add_argument('-M', type=int, default=1,
                    help='Maximum number of avenues')
parser.add_argument('-S', type=int, default=1,
                    help='Maximum number of supermarkets')
parser.add_argument('-C', type=int, default=1,
                    help='Maximum number of houses')
parser.add_argument('-Z', '--seed', type=int, default=None,
                    help='Seed por the rng')                    
                    
opts = parser.parse_args()

random.seed(opts.seed)
N = opts.N
M = opts.M
S = opts.S
C = opts.C

print M, N
print S, C
for _ in range(S):
  print random.randint(1,M), random.randint(1,N)
for _ in range(C):
  print random.randint(1,M), random.randint(1,N)
