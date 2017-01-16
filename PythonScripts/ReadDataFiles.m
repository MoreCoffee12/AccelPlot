clear all
close all
clc

strFileName = 'Trace04_0000003.dat';

fileID = fopen(strFileName);
A = fread(fileID, 'single', 0, 'b');