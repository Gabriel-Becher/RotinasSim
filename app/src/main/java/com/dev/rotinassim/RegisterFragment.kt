package com.dev.rotinassim

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        if( container == null ){
            return null
        }

        val campoEmail = view.findViewById<EditText>(R.id.inputEmailRegistro)
        val campoSenha = view.findViewById<EditText>(R.id.inputSenhaRegistro)
        val campoRepetirSenha = view.findViewById<EditText>(R.id.inputRepeatSenhaRegistro)
        val botaoCadastro = view.findViewById<MaterialButton>(R.id.botaoCadastro)

        botaoCadastro.setOnClickListener {
            val email: String = campoEmail.text.toString()
            val senha: String = campoSenha.text.toString()
            val repetirSenha: String = campoRepetirSenha.text.toString()
            if (email.isBlank() || senha.isBlank() || repetirSenha.isBlank()){
                MaterialAlertDialogBuilder(container.context).setMessage("Campos vazios").setPositiveButton("Ok", null).show()
                return@setOnClickListener
            }

            if(senha != repetirSenha){
                MaterialAlertDialogBuilder(container.context).setMessage("Senhas não são iguais").setPositiveButton("Ok", null).show()
                campoRepetirSenha.text.clear()
            }else{
                if(cadastro(email, senha)){
                    MaterialAlertDialogBuilder(container.context).setMessage("Cadastro Realizado").setPositiveButton("Ok", null).show()

                }
                campoEmail.text.clear()
                campoSenha.text.clear()
                campoRepetirSenha.text.clear()
            }



        }
        return view
    }

    fun cadastro(email: String, senha: String): Boolean{
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}