package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.objecthunter.exp4j.ExpressionBuilder



class Calculator : AppCompatActivity() {

    private lateinit var vivod: TextView; private lateinit var row: String
    private lateinit var num1: Button; private lateinit var num2: Button
    private lateinit var num3: Button; private lateinit var num4: Button
    private lateinit var num5: Button; private lateinit var num6: Button
    private lateinit var num7: Button; private lateinit var num8: Button
    private lateinit var num9: Button; private lateinit var num0: Button
    private lateinit var butP: Button; private lateinit var butC: Button
    private lateinit var bracket1: Button; private lateinit var bracket2: Button
    private lateinit var plusB: Button; private lateinit var minusB: Button
    private lateinit var umnB: Button; private lateinit var delitB: Button
    private lateinit var ravnoB: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        vivod = findViewById(R.id.vivod); row = ""
        num1 = findViewById(R.id.num1); num2 = findViewById(R.id.num2)
        num3 = findViewById(R.id.num3); num4 = findViewById(R.id.num4)
        num5 = findViewById(R.id.num5); num6 = findViewById(R.id.num6)
        num7 = findViewById(R.id.num7); num8 = findViewById(R.id.num8)
        num9 = findViewById(R.id.num9); num0 = findViewById(R.id.num0)
        butP = findViewById(R.id.butP); butC = findViewById(R.id.butC)
        bracket1 = findViewById(R.id.bracket1); bracket2 = findViewById(R.id.bracket2)
        plusB = findViewById(R.id.plus); minusB = findViewById(R.id.minus)
        umnB = findViewById(R.id.umn); delitB = findViewById(R.id.delit)
        ravnoB = findViewById(R.id.ravno)
    }

    override fun onResume() {
        super.onResume()
        num1.setOnClickListener{row += 1; vivod.text = row}; num2.setOnClickListener{row += 2; vivod.text = row}
        num3.setOnClickListener{row += 3; vivod.text = row}; num4.setOnClickListener{row += 4; vivod.text = row}
        num5.setOnClickListener{row += 5; vivod.text = row}; num6.setOnClickListener{row += 6; vivod.text = row}
        num7.setOnClickListener{row += 7; vivod.text = row}; num8.setOnClickListener{row += 8; vivod.text = row}
        num9.setOnClickListener{row += 9; vivod.text = row}; num0.setOnClickListener{row += 0; vivod.text = row}
        butP.setOnClickListener{row += "."; vivod.text = row}; butC.setOnClickListener{row = ""; vivod.text = row}
        bracket1.setOnClickListener{row += "("; vivod.text = row}; bracket2.setOnClickListener{row += ")"; vivod.text = row}
        plusB.setOnClickListener{row += "+"; vivod.text = row}; minusB.setOnClickListener{row += "-"; vivod.text = row}
        umnB.setOnClickListener{row += "*"; vivod.text = row}; delitB.setOnClickListener{row += "/"; vivod.text = row}
        ravnoB.setOnClickListener{
            try {
                if(row.matches(".*[+\\-*/]{2,}.*".toRegex()) || (row.lastOrNull() ?: '0') in "+-*/"){
                    vivod.text = "Ошибка"
                    return@setOnClickListener
                }
                val expression = ExpressionBuilder(row).build()
                val otvet = expression.evaluate()
                vivod.text = otvet.toString()
                row = otvet.toString()
            }
            catch (e: Exception) {
                vivod.text = "Ошибка"
            }
        }

    }
}

