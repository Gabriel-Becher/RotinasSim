package com.dev.rotinassim

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dev.rotinassim.api.RetrofitInstance
import com.dev.rotinassim.api.models.User
import com.dev.rotinassim.databinding.FragmentLoginBinding
import com.dev.rotinassim.room.DatabaseRoom
import com.dev.rotinassim.room.entities.UserLocal
import com.dev.rotinassim.utils.CacheUtils
import com.dev.rotinassim.utils.PrefsUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        if(container == null){
            return null
        }

        binding.botaoLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val senha = binding.senhaLogin.text.toString()

            lifecycleScope.launch {
                val dao = DatabaseRoom.getDatabase(requireContext()).userLocalDao()
                val localUser = dao.getUserByEmail(email)
                val context = requireContext()
                if (localUser != null) {
                    if (localUser.password == senha) {
                        CacheUtils.escreverUsuarioCache(
                            context,
                            localUserData = UserLocal(localUser.id,localUser.email,localUser.password),
                        )
                        PrefsUtils.setUserId(context, localUser.id)
                        startActivity(Intent(context, PaginaPrincipal::class.java))
                        requireActivity().finish()
                    } else {
                        MaterialAlertDialogBuilder(context)
                            .setMessage("Senha incorreta")
                            .setPositiveButton("Ok", null)
                            .show()
                    }
                } else {
                    login(email, senha) { sucesso, usuario ->
                        if (sucesso && usuario != null && usuario.id != null) {
                            lifecycleScope.launch {
                                dao.criarUsuario(
                                    UserLocal(usuario.id, usuario.email, usuario.password)
                                )
                                CacheUtils.escreverUsuarioCache(
                                    context,
                                    localUserData = UserLocal(usuario.id,usuario.email,usuario.password),
                                )
                                PrefsUtils.setUserId(context, usuario.id)
                                startActivity(Intent(context, PaginaPrincipal::class.java))
                                requireActivity().finish()
                            }
                        } else {
                            MaterialAlertDialogBuilder(context)
                                .setMessage("Usuário não encontrado")
                                .setPositiveButton("Ok", null)
                                .show()
                        }
                    }
                }
            }
        }

        return view
    }

    fun login(email: String, senha: String, callback: (Boolean, User?) -> Unit) {
        val call = RetrofitInstance.INSTANCE.login(User(null, email, senha))
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    callback(true, response.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                callback(false, null)
            }
        })
    }
}