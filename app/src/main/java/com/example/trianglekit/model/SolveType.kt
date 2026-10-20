package com.example.trianglekit.model

enum class SolveType(val displayName: String, val subtitle: String) {
    SSS("SSS", "Three Sides"),
    SAS("SAS", "Two Sides + Included Angle"),
    ASA("ASA", "Two Angles + Included Side"),
    AAS("AAS", "Two Angles + One Side"),
    SSA("SSA", "Two Sides + Non-Included Angle"),
    RIGHT_TRIANGLE("Right Triangle", "Right-angle solver"),
    BASE_HEIGHT("Base + Height", "Calculate area"),
    AUTO("Auto Solve", "Flexible triangle solver")
}
