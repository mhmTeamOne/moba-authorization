package com.mhm;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Getter
@Setter
public class LombokTest {
    private String name;
    private int age;
    
    @Test
    public void testLombokGettersAndSetters() {
        LombokTest test = new LombokTest();
        test.setName("Test User");
        test.setAge(25);
        
        assertEquals("Test User", test.getName());
        assertEquals(25, test.getAge());
        
        System.out.println("✅ Lombok is working correctly!");
        System.out.println("Name: " + test.getName() + ", Age: " + test.getAge());
    }
} 