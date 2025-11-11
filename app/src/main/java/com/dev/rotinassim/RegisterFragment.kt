package com.dev.rotinassim

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.room.Database
import com.dev.rotinassim.api.ApiService
import com.dev.rotinassim.api.RetrofitInstance
import com.dev.rotinassim.api.models.User
import com.dev.rotinassim.databinding.FragmentRegisterBinding
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.dao.UserLocalDAO
import com.dev.rotinassim.room.entities.UserLocal
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        if( container == null ){
            return null
        }
        binding.botaoCadastro.setOnClickListener {
            val email: String = binding.inputEmailRegistro.text.toString()
            val senha: String = binding.inputSenhaRegistro.text.toString()
            val repetirSenha: String = binding.inputRepeatSenhaRegistro.text.toString()
            if (email.isBlank() || senha.isBlank() || repetirSenha.isBlank()){
                MaterialAlertDialogBuilder(container.context).setMessage("Campos vazios").setPositiveButton("Ok", null).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if(senha.isBlank() || senha.length < 4 || repetirSenha.isBlank() || repetirSenha.length <4){
                    MaterialAlertDialogBuilder(container.context)
                        .setMessage("Senha deve ter no mínimo 4 caracteres")
                        .setPositiveButton("Ok", null)
                        .show()
                    binding.inputSenhaRegistro.text?.clear()
                    binding.inputRepeatSenhaRegistro.text?.clear()
                    return@launch
                }
                if (senha != repetirSenha) {
                    MaterialAlertDialogBuilder(container.context)
                        .setMessage("Senhas não são iguais")
                        .setPositiveButton("Ok", null)
                        .show()
                    binding.inputRepeatSenhaRegistro.text?.clear()
                } else {
                    cadastro(email, senha) { sucesso, usuario ->
                        if (sucesso && usuario != null && usuario.id != null) {
                            lifecycleScope.launch {
                                try {
                                    // Salvar no banco room se o cadastro der certo
                                    DatabaseRoom.getDatabase(requireContext())
                                        .userLocalDao()
                                        .criarUsuario(
                                            UserLocal(usuario.id, usuario.email, usuario.password)
                                        )

                                    MaterialAlertDialogBuilder(container.context)
                                        .setMessage("Cadastro Realizado")
                                        .setPositiveButton("Ok", null)
                                        .show()
                                    binding.inputEmailRegistro.text?.clear()
                                    binding.inputSenhaRegistro.text?.clear()
                                    binding.inputRepeatSenhaRegistro.text?.clear()
                                } catch (e: Exception) {
                                    Log.e("DB_ERROR", e.message.toString())
                                }
                            }
                        } else {
                            MaterialAlertDialogBuilder(container.context)
                                .setMessage("Erro ao cadastrar")
                                .setPositiveButton("Ok", null)
                                .show()
                        }
                    }
                }
            }

        }
        return view
    }

    fun cadastro(email: String, senha: String, callback: (Boolean, User?) -> Unit){
        val cal = RetrofitInstance.INSTANCE.criarUsuario(User(id=null, email=email, password = senha))
        cal.enqueue(object: Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    val corpo = response.body()
                    Log.i("corpo", corpo.toString())
                    callback(true, corpo)
                }else{
                    Log.e("Erro", response.message().toString())
                    callback(false, null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ERRO", t.message.toString())
                callback(false, null)
            }
        })
    }
}