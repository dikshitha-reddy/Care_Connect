import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { Calendar, Clock, User, FileText, Send, CheckCircle, Loader2 } from 'lucide-react';
import { toast } from 'react-toastify';

const PatientDashboard = () => {
  const { user } = useAuth();
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingData, setBookingData] = useState({
    doctorId: '',
    appointmentDate: '',
    appointmentTime: '',
    notes: ''
  });

  const fetchData = async () => {
    try {
      const [docsRes, apptsRes] = await Promise.all([
        api.get('/patient/doctors'),
        api.get('/patient/appointments', { params: { patientId: user.id } })
      ]);
      setDoctors(docsRes.data);
      setAppointments(apptsRes.data);
    } catch (error) {
      toast.error('Failed to fetch data');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleBookAppointment = async (e) => {
    e.preventDefault();
    setIsBooking(true);
    try {
      await api.post('/patient/appointments', bookingData, { params: { patientId: user.id } });
      toast.success('Appointment booked successfully!');
      fetchData(); // Refresh list
      setBookingData({ doctorId: '', appointmentDate: '', appointmentTime: '', notes: '' });
    } catch (error) {
      toast.error(error.response?.data || 'Booking failed');
    } finally {
      setIsBooking(false);
    }
  };

  if (isLoading) return <div className="flex justify-center py-20"><Loader2 className="animate-spin h-10 w-10 text-brand-600" /></div>;

  return (
    <div className="space-y-12 px-4 pb-20">
      <header className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h1 className="text-4xl font-black text-slate-900 tracking-tighter">Patient Dashboard</h1>
          <p className="text-slate-500 font-medium mt-1">Manage your medical appointments and history</p>
        </div>
        <div className="flex items-center space-x-3 bg-white p-2 rounded-2xl shadow-sm border border-slate-100">
           <div className="h-10 w-10 bg-brand-50 rounded-xl flex items-center justify-center">
              <User className="h-5 w-5 text-brand-600" />
           </div>
           <div className="pr-4">
              <p className="text-xs font-black text-slate-400 uppercase tracking-widest">Logged in as</p>
              <p className="text-sm font-bold text-slate-700">{user.name}</p>
           </div>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        {/* Booking Form */}
        <div className="lg:col-span-4">
          <div className="glass p-10 rounded-[2.5rem] sticky top-32">
            <h2 className="text-2xl font-black text-slate-900 mb-8 flex items-center">
              <div className="h-10 w-10 bg-brand-100 rounded-xl flex items-center justify-center mr-4 shadow-sm">
                <Calendar className="h-5 w-5 text-brand-600" />
              </div>
              Book Visit
            </h2>
            <form onSubmit={handleBookAppointment} className="space-y-6">
              <div>
                <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Select Doctor</label>
                <select
                  required
                  value={bookingData.doctorId}
                  onChange={(e) => setBookingData({ ...bookingData, doctorId: e.target.value })}
                  className="block w-full px-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium text-slate-900"
                >
                  <option value="">Choose a specialist</option>
                  {doctors.map(doc => (
                    <option key={doc.id} value={doc.id}>{doc.name} — {doc.specialization}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Date</label>
                  <input
                    type="date"
                    required
                    value={bookingData.appointmentDate}
                    onChange={(e) => setBookingData({ ...bookingData, appointmentDate: e.target.value })}
                    className="block w-full px-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium text-slate-900"
                  />
                </div>
                <div>
                  <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Time</label>
                  <input
                    type="time"
                    required
                    value={bookingData.appointmentTime}
                    onChange={(e) => setBookingData({ ...bookingData, appointmentTime: e.target.value })}
                    className="block w-full px-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium text-slate-900"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Symptoms/Notes</label>
                <textarea
                  value={bookingData.notes}
                  onChange={(e) => setBookingData({ ...bookingData, notes: e.target.value })}
                  className="block w-full px-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium text-slate-900"
                  rows="3"
                  placeholder="Tell us how you feel..."
                ></textarea>
              </div>
              <button
                type="submit"
                disabled={isBooking}
                className="w-full py-4 bg-brand-600 text-white rounded-2xl font-black text-lg hover:bg-brand-700 shadow-xl shadow-brand-100 transition-all disabled:opacity-50 active:scale-95"
              >
                {isBooking ? <Loader2 className="animate-spin mx-auto h-6 w-6" /> : 'Confirm Appointment'}
              </button>
            </form>
          </div>
        </div>

        {/* Appointments List */}
        <div className="lg:col-span-8">
          <div className="glass p-10 rounded-[2.5rem]">
            <h2 className="text-2xl font-black text-slate-900 mb-8 flex items-center">
              <div className="h-10 w-10 bg-emerald-100 rounded-xl flex items-center justify-center mr-4 shadow-sm">
                <Clock className="h-5 w-5 text-emerald-600" />
              </div>
              Your Appointments
            </h2>
            <div className="space-y-6">
              {appointments.length === 0 ? (
                <div className="text-center py-20 bg-slate-50/50 rounded-[2rem] border-2 border-dashed border-slate-200">
                  <Calendar className="h-16 w-16 text-slate-200 mx-auto mb-4" />
                  <p className="text-slate-400 font-bold text-lg">No appointments scheduled</p>
                  <p className="text-slate-300 text-sm">Your booked visits will appear here.</p>
                </div>
              ) : (
                appointments.map(appt => (
                  <div key={appt.id} className="p-8 bg-white/50 border border-slate-100 rounded-[2rem] flex flex-col md:flex-row justify-between items-start md:items-center group hover:bg-white hover:shadow-premium transition-all">
                    <div className="flex items-center space-x-6">
                      <div className="h-16 w-16 bg-slate-100 rounded-[1.25rem] flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
                        <User className="h-8 w-8 text-slate-400" />
                      </div>
                      <div>
                        <h4 className="text-xl font-black text-slate-900 tracking-tight">Dr. {appt.doctor.name}</h4>
                        <p className="text-brand-600 text-sm font-black uppercase tracking-widest">{appt.doctor.specialization}</p>
                        <div className="flex space-x-6 mt-3 text-sm text-slate-400 font-bold">
                           <span className="flex items-center"><Calendar className="h-4 w-4 mr-2" />{appt.appointmentDate}</span>
                           <span className="flex items-center"><Clock className="h-4 w-4 mr-2" />{appt.appointmentTime}</span>
                        </div>
                      </div>
                    </div>
                    <div className="mt-6 md:mt-0 flex items-center space-x-4">
                      <span className={`px-4 py-1.5 rounded-full text-xs font-black tracking-widest uppercase ${
                        appt.status === 'PENDING' ? 'bg-amber-100 text-amber-700' :
                        appt.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' :
                        'bg-rose-100 text-rose-700'
                      }`}>
                        {appt.status}
                      </span>
                      {appt.status === 'COMPLETED' && (
                        <Link
                          to={`/appointments/${appt.id}/prescription`}
                          className="p-3 bg-brand-50 text-brand-600 rounded-xl hover:bg-brand-600 hover:text-white transition-all shadow-sm"
                          title="View Prescription"
                        >
                          <FileText className="h-6 w-6" />
                        </Link>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientDashboard;
