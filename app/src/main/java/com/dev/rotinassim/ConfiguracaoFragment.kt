package com.dev.rotinassim

import android.app.AlarmManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.dev.rotinassim.databinding.FragmentConfiguracaoBinding
import com.dev.rotinassim.utils.PrefsUtils

class ConfiguracaoFragment : Fragment() {

    private var _binding: FragmentConfiguracaoBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfiguracaoBinding.inflate(inflater, container, false)
        val view = binding.root
        val context1 = requireContext()
        val tempo: Int = PrefsUtils.getNotiTime(context1)

        if (container == null){
            return null
        }
        binding.salvarButton.setOnClickListener {
            val selecionado = binding.tempoAntecedencia.selectedItem.toString().split(" ")[0]
            PrefsUtils.setNotiTime(context1, selecionado.toInt())
            //TODO: Implementar logica de alterar workers de notificação
        }
        val opcoes = listOf("1 Minutos","5 Minutos", "10 Minutos", "15 Minutos", "30 Minutos", "60 Minutos")
        val adapter = ArrayAdapter(context1, android.R.layout.simple_spinner_dropdown_item, opcoes)
        binding.tempoAntecedencia.adapter = adapter
        val posicao = (binding.tempoAntecedencia.adapter as ArrayAdapter<String>).getPosition("$tempo Minutos")
        binding.tempoAntecedencia.setSelection(posicao, true)
        return view
    }

}