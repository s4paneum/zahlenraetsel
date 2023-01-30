package io.grpc.examples.helloworld;


import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.*;

public class Riddle {

    HashMap<Character, Integer> mapping;
    int[][] matrix;
    String[][] encodedMatrix;
    int pow = 3;
    public int counter = 0;
    public int id;
    public String server_id;

    Riddle(String server_id, int id){
        this.server_id = server_id;
        this.id = id;
        mapping = generateMapping();
        //System.out.println(mapping);
        matrix = generateRiddle();
        //System.out.println(matrixToString());
        encodedMatrix = encodeRiddle(mapping);
        //System.out.println(riddleToString());

    }

    Riddle(int pow){
        if (pow > 0)
            this.pow = pow;
        mapping = generateMapping();
        System.out.println(mapping);
    }

    String solveStep(){
        counter++;
        return "";
    }

    void brute_force_riddle(String[][] riddle){
        // create mapping
        HashMap<Character, Integer> mapping = new HashMap<>();
        mapping.put('A', 0);
        mapping.put('B', 1);
        mapping.put('C', 2);
        mapping.put('D', 3);
        mapping.put('E', 4);
        mapping.put('F', 5);
        mapping.put('G', 6);
        mapping.put('H', 7);
        mapping.put('I', 8);
        mapping.put('J', 9);
        Integer[] numbers = {0,0,0,0,0,0,0,0,0,0};

        int i = 1;
        int j = 0;

        while (i < numbers.length){
            if (numbers[i] < i) {
                j = i % 2 * numbers[i];

                // swap
                char char_at_j = (char)  ('A' + j);
                char char_at_i = (char)  ('A' + i);
                int tmp = mapping.get(char_at_j);
                mapping.put(char_at_j, mapping.get(char_at_i));
                mapping.put(char_at_i, tmp);

                if (is_solvable(mapping)) {
                    this.mapping = mapping;
                    return;
                }

                numbers[i] =  numbers[i] + 1;
                i = 1;
            } // end if
            else { // (p[i] equals i)
                numbers[i] = 0;
                i++;
            } // end else (p[i] equals i)
        } // end while (i < N)

    System.out.println("no solution found");

    }

    private boolean is_solvable(HashMap<Character, Integer> map){
        int l1_n1 = decodeNumber(encodedMatrix[0][0], map);
        int l1_n2 = decodeNumber(encodedMatrix[0][1], map);
        int l1_sum = decodeNumber(encodedMatrix[0][2], map);

        int l2_n1 = decodeNumber(encodedMatrix[1][0], map);
        int l2_n2 = decodeNumber(encodedMatrix[1][1], map);
        int l2_sum = decodeNumber(encodedMatrix[1][2], map);

        int l3_n1 = decodeNumber(encodedMatrix[2][0], map);
        int l3_n2 = decodeNumber(encodedMatrix[2][1], map);
        int l3_sum = decodeNumber(encodedMatrix[2][2], map);

        if (l1_n1 + l1_n2 != l1_sum)
            return false;

        if (l2_n1 + l2_n2 != l2_sum)
            return false;

        if (l3_n1 + l3_n2 != l3_sum)
            return false;

        if (l1_n1 + l2_n1 != l3_n1)
            return false;

        if (l1_n2 + l2_n2 != l3_n2)
            return false;

        if (l1_sum + l2_sum != l3_sum)
            return false;

        return true;
    }

    HashMap<Character, Integer> generateMapping(){
        HashMap<Character, Integer> mapping = new HashMap<Character, Integer>();

        Integer[] numbers = {0,1,2,3,4,5,6,7,8,9};
        List<Integer> shuffled_numbers = Arrays.asList(numbers);
        Collections.shuffle(shuffled_numbers);

        char c = 'A';
        for (int i = 0; i < shuffled_numbers.size(); i++) {
            mapping.put(c,shuffled_numbers.get(i));
            c++;
        }

        return mapping;
    }

    int[][] generateRiddle(){
        int[][] matrix = generateMatrix();

        return matrix;
    }

    int[][] generateMatrix(){
        int first_n1 = getRandomInteger((int) Math.pow(10,pow));
        int first_n2 = getRandomInteger((int) Math.pow(10,pow));
        int second_n1 = getRandomInteger((int) Math.pow(10,pow));

        int right_sum = first_n1 + first_n2;
        int bot_sum = first_n1 + second_n1;

        int second_n2 = getRandomInteger((int) Math.pow(10,pow));
        int right_sum_2 = second_n1 + second_n2;

        int bot_sum_2 = first_n2 + second_n2;
        int end_sum = bot_sum + bot_sum_2;

        int[] first_row = { first_n1, first_n2, right_sum};
        int[] second_row = {second_n1, second_n2, right_sum_2};
        int[] third_row = {bot_sum, bot_sum_2, end_sum};
        int[][] matrix = {first_row, second_row, third_row};

        return matrix;
    }

    String[][] encodeRiddle(HashMap<Character,Integer> mapping){
        String[][] rows = new String[3][3];
        for (int i = 0; i < 3; i++) {
            String[] row = new String[]{encodeNumber(matrix[i][0], mapping),
                                        encodeNumber(matrix[i][1], mapping),
                                        encodeNumber(matrix[i][2], mapping)};
            rows[i] = row;
        }

        return rows;
    }

    String encodeNumber(int number, HashMap<Character,Integer> mapping){
        String s = "";
        LinkedList<Integer> stack = new LinkedList<Integer>();
        while (number > 0) {
            stack.push( number % 10 );
            number = number / 10;
        }

        while (!stack.isEmpty()) {
            s += getKeyByValue(stack.pop(), mapping);
        }
        return s;
    }

    int decodeNumber(String cypher, HashMap<Character, Integer> mapping){
        String s = "";

        for (char c: cypher.toCharArray()) {
            s += mapping.get(c);
        }

        return Integer.parseInt(s);
    }

    String riddleToString(){
        String s = "";
        for (int i = 0; i < 3; i++) {
            s += encodedMatrix[i][0] + " + " +  encodedMatrix[i][1] + " = " + encodedMatrix[i][2];
            s += "\n";
            if (i < 2){
                s += " + " +  "    +   " + "  = ";
                s += "\n";
            }
        }

        return s;
    }

    // data string for transport
    String encodedRiddleToDataString(){
        String s = "{\n";
        s+= "\"server_id\" : \"" + server_id + "\",\n";
        s+= "\"raetsel_id\" : \"" + id + "\",\n";
        s+= encodedMatrixToString();
        s+= "}";

        return s;
    }

    String decodeRiddle(){
        String s = "";
        for (int i = 0; i < 3; i++) {
            s += decodeNumber(encodedMatrix[i][0], mapping) + " + "
                    +  decodeNumber(encodedMatrix[i][1], mapping) + " = "
                    + decodeNumber(encodedMatrix[i][2], mapping);
            s += "\n";
            if (i < 2){
                s += " + " +  "    +   " + "  = " ;
                s += "\n";
            }
        }

        return s;
    }

    void stringToRiddle(String riddleString){
        JSONObject obj = new JSONObject(riddleString);
        id = obj.getInt("raetsel_id");
        server_id = obj.getString("raetsel_id");

        JSONArray arr = obj.getJSONArray("row1");
        JSONArray arr2 = obj.getJSONArray("row2");
        JSONArray arr3 = obj.getJSONArray("row3");
        for (int i = 0; i < 3; i++) {
            encodedMatrix[0][i] = arr.getString(i);
            encodedMatrix[1][i] = arr2.getString(i);
            encodedMatrix[2][i] = arr3.getString(i);
        }
    }

    String encodedMatrixToString(){
        String s = "";
        for (int i = 0; i < 3; i++) {
            s += "\"row" + (i+1) + "\" : [ \"";
            s += encodedMatrix[i][0] + "\" , \"" +  encodedMatrix[i][1] + "\" , \"" + encodedMatrix[i][2];
            if (i < 2)
                s += "\" ],\n";
            else
                s += "\" ]\n";
        }

        return s;
    }

    String matrixToString(){
        String s = "";
        for (int i = 0; i < 3; i++) {
            s += "row" + (i+1) + " : [ ";
            s += matrix[i][0] + " , " +  matrix[i][1] + " , " + matrix[i][2];
            s += " ],\n";
        }

        return s;
    }

    char getKeyByValue(int value, HashMap<Character,Integer> mapping ) {
        for (Map.Entry<Character, Integer> entry : mapping.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }

        return '?';
    }

    int getRandomInteger(int max){
        return (int) (Math.random() * max);
    }
}
