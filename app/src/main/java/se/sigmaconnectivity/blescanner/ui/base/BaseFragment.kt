package se.sigmaconnectivity.blescanner.ui.base

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable


abstract class BaseFragment : Fragment() {

    protected val disposables = CompositeDisposable()
    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }
}