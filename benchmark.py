#!/usr/bin/env python3
import os

zxys = [
    [12, 1175, 1570],
    [12, 1175, 1571],
    [12, 1176, 1570],
    [12, 1176, 1571],
    [12, 1177, 1570],
    [12, 1177, 1571],
]

# write times
os.remove("rs_times.txt")
for i in range(1):
    for z, x, y in zxys:
        os.system(f"java -jar rastersource/target/scala-2.12/mosaictest-assembly-0.1.0-SNAPSHOT.jar {z} {x} {y} >> rs_times.txt")

os.remove("rio_times.txt")
for i in range(1):
    for z, x, y in zxys:
        os.system(f"python rio/main.py {z} {x} {y} >> rio_times.txt")


# read and calculate times
with open('rs_times.txt') as f:
    times = [float(line.rstrip()) for line in f]
    average_rs_time = sum(times) / len(times)

with open('rio_times.txt') as f:
    times = [float(line.rstrip()) for line in f]
    average_rio_time = sum(times) / len(times)


print(f"Average RS mosaiced read time: {average_rs_time}")
print(f"Average Rio mosaiced read time: {average_rio_time}")
